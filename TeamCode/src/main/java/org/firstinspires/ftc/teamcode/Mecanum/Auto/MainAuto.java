package org.firstinspires.ftc.teamcode.Mecanum.Auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Mecanum.Grabber;
import org.firstinspires.ftc.teamcode.Mecanum.Intake;

import org.firstinspires.ftc.teamcode.Mecanum.Gyroscope;

import java.util.List;

import FTCEngine.Core.Auto.AutoOpModeBase;
import FTCEngine.Core.Behavior;
import FTCEngine.Core.Input;
import FTCEngine.Math.Mathf;
import FTCEngine.Math.Vector2;

@Autonomous(name = "MainAuto")
public class MainAuto extends AutoOpModeBase
{
    @Override
    protected void awake() {
        super.awake();
        getInput().registerButton(Input.Source.CONTROLLER_1, Input.Button.B);
        getInput().registerButton(Input.Source.CONTROLLER_1, Input.Button.X);
        getInput().registerButton(Input.Source.CONTROLLER_1, Input.Button.Y);
    }

    @Override
	public void addBehaviors(List<Behavior> behaviorList)
	{
		behaviorList.add(new Gyroscope(this));
		behaviorList.add(new DrivetrainAuto(this));
		behaviorList.add(new Intake(this));
		behaviorList.add(new IntakeAuto(this));
		behaviorList.add(new LiftAuto(this));
        behaviorList.add(new Grabber(this));
        behaviorList.add(new GrabberAuto(this));
	}

    private Mode mode = Mode.POSITION_1_FULL;
    private int waitTime = 20;

    @Override
    protected void configLoop() {
        super.configLoop();

        if (getInput().getButtonDown(Input.Source.CONTROLLER_1, Input.Button.B)) mode = mode.getNext();

        if (getInput().getButtonDown(Input.Source.CONTROLLER_1, Input.Button.X)) waitTime+=2;
        if (getInput().getButtonDown(Input.Source.CONTROLLER_1, Input.Button.Y)) waitTime-=2;

        waitTime = Mathf.clamp(waitTime,0,30);

        telemetry.addData("Mode (B)",mode);
        telemetry.addData("Wait time (X/Y)", waitTime);
    }

    @Override
	protected void queueJobs()
	{
		DrivetrainAuto drivetrain = getBehavior(DrivetrainAuto.class);
		IntakeAuto intake = getBehavior(IntakeAuto.class);

//		execute(drivetrain, new DrivetrainAuto.AutoJob(DrivetrainAuto.AutoJob.Mode.MOVE, 24f));
//		execute(drivetrain, new DrivetrainAuto.AutoJob(DrivetrainAuto.AutoJob.Mode.STRAFE, 24f));
//		execute(drivetrain, new DrivetrainAuto.AutoJob(DrivetrainAuto.AutoJob.Mode.ROTATE, 90f));


//		execute(drivetrain, new DrivetrainAuto.AutoJob(DrivetrainAuto.AutoJob.Mode.MOVE, 24f));
//
//		//ALIGNMENT
//		wait(0.2f);
//		execute(drivetrain, new DrivetrainAuto.AutoJob(DrivetrainAuto.AutoJob.Mode.ROTATE,-90f));

        if(mode == Mode.POSITION_1_PARK||mode == Mode.POSITION_2_PARK) {
            wait((float) waitTime);
            execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, mode == Mode.POSITION_1_PARK?-33f:33f)));
            return;
        }

        execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, 15f)));
        execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, -15f)));

        wait(.5f);

        execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(-39f, 0f)));
        execute(intake, new IntakeAuto.AutoJob(1f));

        //COLLECTION
        wait(0.2f);
        execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, 10f)));
        execute(intake ,new IntakeAuto.AutoJob(0f));

        wait(0.2f);
        execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(23f, 0f)));

        wait(0.2f);
        execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, -85f)));

        if(mode == Mode.POSITION_1_NO_FOUNDATION) {

            execute(drivetrain, new DrivetrainAuto.AutoJob(180f));
            wait(0.2f);

            execute(intake ,new IntakeAuto.AutoJob(-1f));
            wait(2.0f);

            execute(intake ,new IntakeAuto.AutoJob(0f));
            execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(5f, 0)));
            wait(1.0f);

            execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, -20)));
            return;
        }

        wait(0.2f);
        execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(-15f, 0f)));

        //GRAB PLATFORM
        wait(1f);

        wait(0.2f);
        execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(25f, 0f)));

        //RELEASE PLATFORM
        wait(1f);

        wait(0.2f);
        execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(2f, 0f)));

        //PARK
        wait(0.2f);
        execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, 45f)));






//		wait(1f);
//
//		execute(intake, new IntakeAuto.AutoJob(0f));
//
//		execute(drivetrain, new DrivetrainAuto.AutoJob(DrivetrainAuto.AutoJob.Mode.STRAFE, -63f));
//		execute(intake, new IntakeAuto.AutoJob(-1f));
//
//		wait(5f);
//
//        execute(intake, new IntakeAuto.AutoJob(0f));
//        execute(drivetrain, new DrivetrainAuto.AutoJob(DrivetrainAuto.AutoJob.Mode.MOVE, -7f));
//
//        wait(1f);
//
//		execute(drivetrain, new DrivetrainAuto.AutoJob(DrivetrainAuto.AutoJob.Mode.STRAFE, 30f));
	}

	private enum Mode {

        POSITION_1_FULL(0),
        POSITION_1_NO_FOUNDATION(1),
        POSITION_1_PARK(2),
        POSITION_2_PARK(3);

        Mode(int value) {
            this.value = value;
        }

        private final int value;

        public Mode getNext() {
            return Mode.values()[(value+1)%Mode.values().length];
        }
    }
}
