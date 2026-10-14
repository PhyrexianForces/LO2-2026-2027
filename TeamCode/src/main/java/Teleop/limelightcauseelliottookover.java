package Teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.CRServo;

//Limelight
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;

LimeLight3A limelight;
@override
 public void init() {
     limelight = hardwareMap.get(Limelight3A.class, "limelight");
     limelight.setPollRateHz(100);
     limelight.start();
}   
 

import java.lang.Math;

@TeleOp
public class limelightcauseelliottookover extends OpMode{
 
  @Override
  public void init() {
    
  }



  




  @Override
  public void loop() {
    //Determines the inputs of the gamepad
   
   }
}
