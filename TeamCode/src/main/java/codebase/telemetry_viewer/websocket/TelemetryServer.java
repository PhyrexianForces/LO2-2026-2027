package codebase.telemetry_viewer.websocket;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import codebase.telemetry_viewer.websocket.packets.TelemetryNewConnectionPacket;
import codebase.telemetry_viewer.websocket.packets.TelemetryUpdatePacket;
import launchpad.Loop;
import launchpad.actions.SequentialAction;
import launchpad.geometry.FieldPosition;

public class TelemetryServer extends WebSocketServer implements Loop {

    /**
     * A single readable piece of telemetry state: either an annotated field or an annotated
     * no-arg method (possibly declared on an interface/superclass rather than the concrete class).
     */
    private interface TelemetryTarget {
        Object getValue() throws ReflectiveOperationException;
        Class<?> getValueType();
        String getTelemetryName();
    }

    private static class FieldTarget implements TelemetryTarget {
        final Object owner;
        final Field field;

        FieldTarget(Object owner, Field field) {
            this.owner = owner;
            this.field = field;
            field.setAccessible(true);
        }

        @Override
        public Object getValue() throws IllegalAccessException {
            return field.get(owner);
        }

        @Override
        public Class<?> getValueType() {
            return field.getType();
        }

        @Override
        public String getTelemetryName() {
            return field.getName();
        }
    }

    private static class MethodTarget implements TelemetryTarget {
        final Object owner;
        final Method method;

        MethodTarget(Object owner, Method method) {
            this.owner = owner;
            this.method = method;
            method.setAccessible(true);
        }

        @Override
        public Object getValue() throws ReflectiveOperationException {
            return method.invoke(owner);
        }

        @Override
        public Class<?> getValueType() {
            return method.getReturnType();
        }

        @Override
        public String getTelemetryName() {
            String name = method.getName();
            if (name.length() > 3 && name.startsWith("get") && Character.isUpperCase(name.charAt(3))) {
                return Character.toLowerCase(name.charAt(3)) + name.substring(4);
            }
            return name;
        }
    }

    /** Wraps a TelemetryTarget to report under an explicit display name instead of its derived one. */
    private static class NamedTelemetryTarget implements TelemetryTarget {
        private final TelemetryTarget delegate;
        private final String name;

        NamedTelemetryTarget(TelemetryTarget delegate, String name) {
            this.delegate = delegate;
            this.name = name;
        }

        @Override
        public Object getValue() throws ReflectiveOperationException {
            return delegate.getValue();
        }

        @Override
        public Class<?> getValueType() {
            return delegate.getValueType();
        }

        @Override
        public String getTelemetryName() {
            return name;
        }
    }

    private final OpMode opMode;
    private final List<TelemetryTarget> telemetryDataTargets;
    private final List<TelemetryTarget> robotPositionTargets;
    private final List<TelemetryTarget> pendingTelemetryObjectTargets;

    public TelemetryServer(OpMode opMode) {
        super(new InetSocketAddress(51631));
        this.opMode = opMode;
        this.telemetryDataTargets = new ArrayList<>();
        this.robotPositionTargets = new ArrayList<>();
        this.pendingTelemetryObjectTargets = new ArrayList<>();

        scanObject(opMode);

        this.setReuseAddr(true);
        this.setTcpNoDelay(true);
    }

    private void scanObject(Object obj) {
        if (obj == null) return;

        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            registerAnnotatedMember(field, new FieldTarget(obj, field));
        }

        for (Method method : findAnnotatedMethods(obj.getClass())) {
            registerAnnotatedMember(method, new MethodTarget(obj, method));
        }
    }

    private void registerAnnotatedMember(AccessibleObject member, TelemetryTarget target) {
        // A RobotPosition target is sent as its own packet type, so it must not also be
        // registered as a plain TelemetryData target (that would send it twice).
        if (member.isAnnotationPresent(RobotPosition.class)) {
            String displayName = member.getAnnotation(RobotPosition.class).value();
            robotPositionTargets.add(displayName.isEmpty() ? target : new NamedTelemetryTarget(target, displayName));
        } else if (member.isAnnotationPresent(TelemetryData.class)) {
            String displayName = member.getAnnotation(TelemetryData.class).value();
            telemetryDataTargets.add(displayName.isEmpty() ? target : new NamedTelemetryTarget(target, displayName));
        }
        if (member.isAnnotationPresent(TelemetryObject.class)) {
            resolveTelemetryObjectTarget(target);
        }
    }

    /**
     * Finds no-arg methods carrying a telemetry annotation, including ones declared on an
     * interface or superclass but only ever overridden (without repeating the annotation) by
     * the concrete class. The returned Method objects still dispatch virtually to the concrete
     * override when invoked, so it's safe to invoke them directly on the runtime instance.
     */
    private List<Method> findAnnotatedMethods(Class<?> clazz) {
        List<Method> found = new ArrayList<>();
        Set<String> claimedNames = new HashSet<>();
        collectAnnotatedMethods(clazz, found, claimedNames);
        return found;
    }

    private void collectAnnotatedMethods(Class<?> clazz, List<Method> found, Set<String> claimedNames) {
        if (clazz == null) return;

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getParameterCount() != 0) continue;
            if (claimedNames.contains(method.getName())) continue;

            if (method.isAnnotationPresent(TelemetryData.class)
                    || method.isAnnotationPresent(RobotPosition.class)
                    || method.isAnnotationPresent(TelemetryObject.class)) {
                method.setAccessible(true);
                found.add(method);
                claimedNames.add(method.getName());
            }
        }

        for (Class<?> iface : clazz.getInterfaces()) {
            collectAnnotatedMethods(iface, found, claimedNames);
        }

        collectAnnotatedMethods(clazz.getSuperclass(), found, claimedNames);
    }

    /**
     * Scans a {@code @TelemetryObject} target's current value if it's set, or queues the target
     * to be retried on later loops if it's still null (e.g. hardware wired up after init()).
     */
    private void resolveTelemetryObjectTarget(TelemetryTarget target) {
        try {
            Object nested = target.getValue();
            if (nested != null) {
                scanObject(nested);
            } else {
                pendingTelemetryObjectTargets.add(target);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void resolvePendingTelemetryObjects() {
        if (pendingTelemetryObjectTargets.isEmpty()) return;

        List<TelemetryTarget> stillPending = new ArrayList<>();
        List<TelemetryTarget> nowResolved = new ArrayList<>();

        for (TelemetryTarget target : pendingTelemetryObjectTargets) {
            try {
                if (target.getValue() != null) {
                    nowResolved.add(target);
                } else {
                    stillPending.add(target);
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        pendingTelemetryObjectTargets.clear();
        pendingTelemetryObjectTargets.addAll(stillPending);

        // Scanned after pendingTelemetryObjectTargets is back in a consistent state, since
        // scanning a newly-resolved object can itself queue more pending targets.
        for (TelemetryTarget target : nowResolved) {
            try {
                scanObject(target.getValue());
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void loop() {
        resolvePendingTelemetryObjects();
        sendUpdatePackets();
    }

    private void sendUpdatePackets() {
        for (TelemetryTarget target : telemetryDataTargets) {
            sendPacketForTarget(target, false);
        }
        for (TelemetryTarget target : robotPositionTargets) {
            sendPacketForTarget(target, true);
        }
    }

    private void sendPacketForTarget(TelemetryTarget target, boolean isRobotPosition) {
        Object fieldValue;
        try {
            fieldValue = target.getValue();
        } catch (Exception e) {
            // A telemetry getter throwing (e.g. reading an unconfigured sensor) must never take
            // down the OpMode's loop() — report the failure as a value instead of propagating it.
            Throwable cause = (e instanceof InvocationTargetException && e.getCause() != null) ? e.getCause() : e;
            sendErrorPacket(target, cause);
            return;
        }
        Class<?> fieldType = target.getValueType();

        TelemetryUpdatePacket packet = new TelemetryUpdatePacket();
        packet.telemetryDataName = target.getTelemetryName();

        if (isRobotPosition && fieldType == FieldPosition.class) {
            packet.telemetryDataType = TelemetryUpdatePacket.TelemetryDataType.ROBOT_POSITION;
            if (fieldValue != null) {
                packet.telemetryDataValue = new TelemetryUpdatePacket.TelemetryFieldPosition((FieldPosition) fieldValue);
            }
        } else if (fieldType == Double.class || fieldType == double.class || fieldType == Float.class || fieldType == float.class) {
            packet.telemetryDataType = TelemetryUpdatePacket.TelemetryDataType.DOUBLE;
            packet.telemetryDataValue = fieldValue;
        } else if (fieldType == String.class) {
            packet.telemetryDataType = TelemetryUpdatePacket.TelemetryDataType.STRING;
            packet.telemetryDataValue = fieldValue;
        } else if (fieldType == FieldPosition.class) {
            packet.telemetryDataType = TelemetryUpdatePacket.TelemetryDataType.FIELD_POSITION;
            if (fieldValue != null) {
                packet.telemetryDataValue = new TelemetryUpdatePacket.TelemetryFieldPosition((FieldPosition) fieldValue);
            }
        } else if (fieldType == SequentialAction.class) {
            packet.telemetryDataType = TelemetryUpdatePacket.TelemetryDataType.ACTION_QUEUE;
            packet.telemetryDataValue = new TelemetryUpdatePacket.TelemetryActionQueue((SequentialAction) fieldValue);
        } else {
            packet.telemetryDataType = TelemetryUpdatePacket.TelemetryDataType.STRING;
            if (fieldValue != null) {
                packet.telemetryDataValue = fieldValue.toString();
            }
        }

        this.broadcast(packet.toJson());
    }

    private void sendErrorPacket(TelemetryTarget target, Throwable cause) {
        TelemetryUpdatePacket packet = new TelemetryUpdatePacket();
        packet.telemetryDataName = target.getTelemetryName();
        packet.telemetryDataType = TelemetryUpdatePacket.TelemetryDataType.STRING;
        packet.telemetryDataValue = "ERROR: " + cause.getClass().getSimpleName()
                + (cause.getMessage() != null ? ": " + cause.getMessage() : "");

        this.broadcast(packet.toJson());
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        webSocket.send(new TelemetryNewConnectionPacket(this.opMode).toJson());
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {}

    @Override
    public void onMessage(WebSocket webSocket, String s) {}

    @Override
    public void onError(WebSocket webSocket, Exception e) {}

    @Override
    public void onStart() {}
}
