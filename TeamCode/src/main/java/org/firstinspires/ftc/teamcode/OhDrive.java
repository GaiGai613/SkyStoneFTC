package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.List;

import FTCEngine.Core.Behavior;
import FTCEngine.Core.Input;
import FTCEngine.Core.Main;
import FTCEngine.Math.Mathf;
import FTCEngine.Math.Vector2;
import FTCEngine.Math.Vector3;

public class OhDrive extends Behavior
{
    /**
     * NOTE: Do not configure the electronics in the constructor, do them in the awake method!
     *
     * @param opMode
     */
    public OhDrive(Main opMode)
    {
        super(opMode);
    }

    @Override
    public void awake(HardwareMap hardwareMap)
    {
        super.awake(hardwareMap);

        gyroscope = opMode.getBehavior(Gyroscope.class);

        motorLeft = hardwareMap.dcMotor.get("motorLeft");
        motorRight = hardwareMap.dcMotor.get("motorRight");
        strafeOne = hardwareMap.dcMotor.get("strafeOne");
        strafeTwo = hardwareMap.dcMotor.get("strafeTwo");

        motorRight.setDirection(DcMotorSimple.Direction.REVERSE);
        strafeOne.setDirection(DcMotorSimple.Direction.REVERSE);

        motorRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        strafeOne.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        strafeTwo.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        motorLeft.setPower(0);
        motorRight.setPower(0);
        strafeOne.setPower(0);
        strafeTwo.setPower(0);
    }

    private Gyroscope gyroscope;
    private DcMotor motorRight;
    private DcMotor motorLeft;
    private DcMotor strafeOne;
    private DcMotor strafeTwo;

    private Vector2 direction;

    @Override
    public void update()
    {
        super.update();

        Vector2 targetAngle = input.getDirection(Input.Source.CONTROLLER_1, Input.Button.LEFT_JOYSTICK); // angle we want to go (i.e. forward)
        Vector2 currentVelocity = gyroscope.getVelocity().toXY(); // gets current velocity

        if(!currentVelocity.equals(Vector2.zero))
        {
            float degree = Vector2.signedAngle(Vector2.right, targetAngle);
            float velocityDegree = Vector2.signedAngle(Vector2.right, currentVelocity);

            float difference = Mathf.toSignedAngle(degree - velocityDegree); // calculates difference in angles

            float sign = Math.signum(difference);
            difference = Math.abs(difference);

            if(difference >= 2f)
            {
                direction.rotate(sign * difference/10); // rotates velocity vector to go striaght
            }

        }
        else {
            direction = Vector2.zero;
        }

        setTargetVector(direction);

    }

    /**
     *
     * @param target
     *
     * Sets motor powers to wanted values
     */

    private void setTargetVector(Vector2 target)
    {
        motorRight.setPower(target.x);
        motorLeft.setPower(target.x);

        strafeOne.setPower(target.y);
        strafeTwo.setPower(target.y);
    }
}