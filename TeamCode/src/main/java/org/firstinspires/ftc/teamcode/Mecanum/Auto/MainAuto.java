package org.firstinspires.ftc.teamcode.Mecanum.Auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Mecanum.Camera;
import org.firstinspires.ftc.teamcode.Mecanum.FoundationGrabber;
import org.firstinspires.ftc.teamcode.Mecanum.Grabber;
import org.firstinspires.ftc.teamcode.Mecanum.Intake;

import org.firstinspires.ftc.teamcode.Mecanum.Gyroscope;

import java.util.List;

import FTCEngine.Core.Auto.AutoOpModeBase;
import FTCEngine.Core.Behavior;
import FTCEngine.Core.Input;
import FTCEngine.Math.Mathf;
import FTCEngine.Math.Vector2;
import FTCEngine.VisionPipeline;

@Autonomous(name = "MainAuto")
public class MainAuto extends AutoOpModeBase
{
	@Override
	protected void awake()
	{
		super.awake();
		getInput().registerButton(Input.Source.CONTROLLER_1, Input.Button.B);
		getInput().registerButton(Input.Source.CONTROLLER_1, Input.Button.X);
		getInput().registerButton(Input.Source.CONTROLLER_1, Input.Button.Y);
	}

	@Override
	public void addBehaviors(List<Behavior> behaviorList)
	{
		behaviorList.add(new Gyroscope(this));
		behaviorList.add(new Camera(this));
		behaviorList.add(new DrivetrainAuto(this));
		behaviorList.add(new TouchSensorAuto(this));
		behaviorList.add(new LiftAuto(this));

		behaviorList.add(new Intake(this));
		behaviorList.add(new IntakeAuto(this));

		behaviorList.add(new Grabber(this));
		behaviorList.add(new GrabberAuto(this));

		behaviorList.add(new FoundationGrabber(this));
		behaviorList.add(new FoundationGrabberAuto(this));
	}

	private Mode mode = Mode.POSITION_1_FULL;
	private int waitTime = 20;

	private final float delay = 0.01f;

	private DrivetrainAuto drivetrain;
	private IntakeAuto intake;
	private GrabberAuto grabber;
	private LiftAuto lift;
	private FoundationGrabberAuto foundationGrabber;
	private TouchSensorAuto touchSensor;

	private float rotation;

	@Override
	protected void configLoop()
	{
		super.configLoop();

		if (getInput().getButtonDown(Input.Source.CONTROLLER_1, Input.Button.B)) mode = mode.getNext();

		if (getInput().getButtonDown(Input.Source.CONTROLLER_1, Input.Button.X)) waitTime += 2;
		if (getInput().getButtonDown(Input.Source.CONTROLLER_1, Input.Button.Y)) waitTime -= 2;

		waitTime = Mathf.clamp(waitTime, 0, 30);

		telemetry.addData("Mode (B)", mode);
		telemetry.addData("Wait time (X/Y)", waitTime);
	}

	private void setup()
	{
		drivetrain = getBehavior(DrivetrainAuto.class);
		intake = getBehavior(IntakeAuto.class);
		grabber = getBehavior(GrabberAuto.class);
		lift = getBehavior(LiftAuto.class);
		foundationGrabber = getBehavior(FoundationGrabberAuto.class);
		touchSensor = getBehavior(TouchSensorAuto.class);
	}

	private void tuneDrivetrain()
	{
//		execute(drivetrain, new DrivetrainAuto.AutoJob(DrivetrainAuto.AutoJob.Mode.MOVE, 24f));
//		execute(drivetrain, new DrivetrainAuto.AutoJob(DrivetrainAuto.AutoJob.Mode.STRAFE, 24f));
//		execute(drivetrain, new DrivetrainAuto.AutoJob(DrivetrainAuto.AutoJob.Mode.ROTATE, 90f));
	}

	private void simplePark()
	{
		wait((float)waitTime);
		execute(foundationGrabber, new FoundationGrabberAuto.AutoJob(FoundationGrabber.Mode.GRABBED)); //out of way of other robot hopefully

		execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(mode == Mode.POSITION_1_PARK ? -24f : 0f, 0f)));
		execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, mode == Mode.POSITION_1_PARK ? (getIsBlue() ? -28f : -15f) : (getIsBlue() ? 15f : -28f))));
	}

	private void setRotation(float rotation, float allowedError, float power)
	{
		this.rotation = rotation;
		execute(drivetrain, new DrivetrainAuto.AutoJob(rotation, allowedError, power));
	}

	private void setRotation(float rotation, float allowedError)
	{
		setRotation(rotation, allowedError, 1f);
	}

	private void setRotation(float rotation)
	{
		setRotation(rotation, 3f, 1f);
	}

	private void resetRotation()
	{
		execute(drivetrain, new DrivetrainAuto.AutoJob(rotation, 2f));
	}

	@Override
	protected void queueJobs()
	{
		setup();
		execute(foundationGrabber, new FoundationGrabberAuto.AutoJob(FoundationGrabber.Mode.GRABBED));

//		tuneDrivetrain();

		if (mode == Mode.POSITION_2_FOUNDATION)
		{
			wait((float)waitTime);
			execute(foundationGrabber, new FoundationGrabberAuto.AutoJob(FoundationGrabber.Mode.RELEASED)); //Puts grabbers up

			execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0, 20))); //Aligns with foundation center

			buffer(drivetrain, new DrivetrainAuto.AutoJob(Vector2.left, 0.5f)); //Moves...
			buffer(touchSensor, new TouchSensorAuto.AutoJob(TouchSensorAuto.AutoJob.Mode.EXIT_WITH_ONE_TOUCHED)); //...until foundation hit
			execute();

			execute(drivetrain, new DrivetrainAuto.AutoJob(Vector2.zero, 0f)); //Stops moving

			//GRAB PLATFORM
			execute(foundationGrabber, new FoundationGrabberAuto.AutoJob(FoundationGrabber.Mode.GRABBED)); //Grabs platform

			wait(0.35f);

			execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(35, -15))); //Moves foundation to building site (Value.x was 28)
			setRotation(-90f, 5f, 0.3f);

			buffer(foundationGrabber, new FoundationGrabberAuto.AutoJob(FoundationGrabber.Mode.FOLDED)); //Releases platform
			execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0, 10f)));

			//Moves to park
			execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0, -5f)));
			setRotation(-180f, 5f, 0.5f);

			buffer(drivetrain, new DrivetrainAuto.AutoJob(Vector2.left, 0.5f)); //Moves...
			buffer(touchSensor, new TouchSensorAuto.AutoJob(TouchSensorAuto.AutoJob.Mode.EXIT_WITH_BOTH_TOUCHED)); //...until hit wall
			execute();

			execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, 1f), 0.8f)); //Moves to park and use power because encoders are not working
			wait(0.8f);

			execute(drivetrain, new DrivetrainAuto.AutoJob(Vector2.zero, 0f));
			return;
		}

		if (mode == Mode.POSITION_1_PARK || mode == Mode.POSITION_2_PARK)
		{
			simplePark();
			return;
		}

		VisionPipeline.Position block = VisionPipeline.Position.UNKNOWN; //getBehavior(Camera.class).getPosition();
		if (!getIsBlue() && mode == Mode.POSITION_1_FULL) mode = Mode.POSITION_1_NO_BLOCKS;

		if (mode == Mode.POSITION_1_NO_BLOCKS)
		{
			execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(-21f, 0f))); // skips blocks and misses parked alliance member
			execute(grabber, new GrabberAuto.AutoJob(false, false));
		}
		else
		{
			float yOffset = 0f;

			if (block == VisionPipeline.Position.CENTER) yOffset = 16f;
			else if (block == VisionPipeline.Position.LEFT) yOffset = 8f;

//			execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, yOffset))); //Line up with skystone

			buffer(grabber, new GrabberAuto.AutoJob(false, false)); //Holds grabber in correct spot
			execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(-38f, 0f))); //Goes to blocks
			execute(foundationGrabber, new FoundationGrabberAuto.AutoJob(FoundationGrabber.Mode.RELEASED));//Puts foundation grabber to middle

			buffer(intake, new IntakeAuto.AutoJob(1f)); //Starts up intake
			buffer(lift, new LiftAuto.AutoJob(1f)); //Lifts lift so intake works
			execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, -5f))); //Full power back drops intake

			execute(lift, new LiftAuto.AutoJob(0f)); //Stops lift
			execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, 12f))); //Drive forward to collect

//			for (int i = 0; i < 4; i++)
//			{
//				execute(intake, new IntakeAuto.AutoJob(i % 2 == 0 ? -1f : 1f)); //Weird thing that worked
//				wait(0.1f);
//			}

			execute(grabber, new GrabberAuto.AutoJob(false, false));
			execute(lift, new LiftAuto.AutoJob(-0.1f)); //Lets lift down
			execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(17f, 0f))); //Moves back to cross under alliance bridge

			resetRotation();
		}

		execute(grabber, new GrabberAuto.AutoJob(true, false)); //Grabs block
		execute(foundationGrabber, new FoundationGrabberAuto.AutoJob(FoundationGrabber.Mode.GRABBED));

		buffer(lift, new LiftAuto.AutoJob(-0.3f)); //Lets lift down
		execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, -1f), 1f)); //Goes to other side and nearly aligns to wall

		float time = 0f;

		if (getIsBlue())
		{
			time = 1.97f;
			if (block == VisionPipeline.Position.CENTER) time += 0.2f;
			else if (block == VisionPipeline.Position.LEFT) time += 0.1f;
		}
		else time = 1.5f;

		wait(time);

		execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, -1f), 0.3f)); //Low power alignment
		execute(foundationGrabber, new FoundationGrabberAuto.AutoJob(FoundationGrabber.Mode.RELEASED)); //Puts foundation grabber to middle (after b/c doesnt hit wall on red)
		wait(getIsBlue() ? 1f : 3f);

		buffer(lift, new LiftAuto.AutoJob(0f)); //Stops lift
		execute(drivetrain, new DrivetrainAuto.AutoJob(Vector2.zero, 0f)); //Stop motors

		execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, getIsBlue() ? 15f : 13f))); //Goes up to foundation from wall

		if (mode != Mode.POSITION_1_FULL) resetRotation(); //no time for such in full auto

		buffer(drivetrain, new DrivetrainAuto.AutoJob(Vector2.left, 0.5f)); //Moves...
		buffer(touchSensor, new TouchSensorAuto.AutoJob(TouchSensorAuto.AutoJob.Mode.EXIT_WITH_ONE_TOUCHED)); //...until foundation hit
		execute();

		execute(drivetrain, new DrivetrainAuto.AutoJob(Vector2.zero, 0f)); //Stops moving

		//GRAB PLATFORM
		execute(foundationGrabber, new FoundationGrabberAuto.AutoJob(FoundationGrabber.Mode.GRABBED)); //Grabs platform
		if (mode == Mode.POSITION_1_FULL) execute(lift, new LiftAuto.AutoJob(1.0f)); //Raises lift

		wait(0.55f);

		execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(40f, 0f))); //Moves foundation to building site (Value x was 28)
		execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, 10f)));

		if (getIsBlue()) setRotation(90f, 10f); //Rotates foundation with full power
		else setRotation(90f, 2f, 0.3f); //Rotates foundation with half power

		buffer(intake, new IntakeAuto.AutoJob(-1f)); //Stops intake
		execute(foundationGrabber, new FoundationGrabberAuto.AutoJob(FoundationGrabber.Mode.RELEASED)); //Lets go of foundation

		if (mode == Mode.POSITION_1_FULL)
		{
			//RELEASE PLATFORM
			execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, 10f))); //Moves away from foundation to rotate

			buffer(grabber, new GrabberAuto.AutoJob(true, true)); //Rotates arm
			setRotation(0f,10f); //Rotates so lift faces foundation

			buffer(lift, new LiftAuto.AutoJob(-0.15f)); //Drops lift down
			execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, -15f))); //Pushes foundation into building zone
		}
		else
		{
			execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(-5f, 0f)));

			execute(lift, new LiftAuto.AutoJob(1f)); //Raises lift so other intake drops
			wait(0.3f);
			execute(lift, new LiftAuto.AutoJob(-0.1f));

			setRotation(0f, 2f, 0.8f);
			wait(0.3f);

			execute(lift, new LiftAuto.AutoJob(0f));
		}

		execute(foundationGrabber, new FoundationGrabberAuto.AutoJob(FoundationGrabber.Mode.GRABBED)); //Puts grabbers down so it doesn't hit bridge when parking

		if (mode == Mode.POSITION_1_FULL)
		{
			execute(lift, new LiftAuto.AutoJob(-0.2f)); //Lowers lift down all the way
			execute(grabber, new GrabberAuto.AutoJob(false, true)); //Releases block

			wait(0.1f);
		}

		//Try park
//		resetRotation();

//		execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(-7f, 0f)));
//		execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(0f, 38f))); //Moves to park

		execute(drivetrain, new DrivetrainAuto.AutoJob(new Vector2(-7f, 38f))); //Moves to park
		execute(lift, new LiftAuto.AutoJob(0.0f));
	}

	private enum Mode
	{
		POSITION_1_FULL(0),
		POSITION_1_NO_BLOCKS(1),
		POSITION_1_PARK(2),
		POSITION_2_PARK(3),
		POSITION_2_FOUNDATION(4);

		Mode(int value)
		{
			this.value = value;
		}

		private final int value;

		public Mode getNext()
		{
			return Mode.values()[(value + 1) % Mode.values().length];
		}
	}
}
