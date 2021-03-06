package com.team4278;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;

/**
 * OpMode superclass which polls the controller buttons and allows the opmode to handle when they are pressed and released.
 *
 * Similar to our old Teleop.c checkJoystickButtons() system.
 *
 * Make sure that subclasses call super.loop() in loop()!!!
 */
public abstract class ButtonListenerTeleop extends SequenceOpMode
{
	/**
	 * Enum representing the buttons on an Xbox controller (or one of its clones)
	 */
	public enum Button
	{
		X,
		Y,
		A,
		B,
		START,
		BACK,
		GUIDE,
		LBUMPER,
		RBUMPER,
		LEFT_STICK,
		RIGHT_STICK,
		DPAD_UP,
		DPAD_RIGHT,
		DPAD_DOWN,
		DPAD_LEFT
	}

	EnumSet<Button> prevButtons;
	
	EnumSet<Button> readButtons()
	{
		EnumSet<Button> buttons = EnumSet.noneOf(Button.class);
		if(gamepad1.x)
			buttons.add(Button.X);
		if(gamepad1.y)
			buttons.add(Button.Y);
		if(gamepad1.a)
			buttons.add(Button.A);
		if(gamepad1.b)
			buttons.add(Button.B);
		if(gamepad1.start)
			buttons.add(Button.START);
		if(gamepad1.back)
			buttons.add(Button.BACK);
		if(gamepad1.guide)
			buttons.add(Button.GUIDE);
		if(gamepad1.left_bumper)
			buttons.add(Button.LBUMPER);
		if(gamepad1.right_bumper)
			buttons.add(Button.RBUMPER);
		if(gamepad1.left_stick_button)
			buttons.add(Button.LEFT_STICK);
		if(gamepad1.right_stick_button)
			buttons.add(Button.RIGHT_STICK);
		if(gamepad1.dpad_up)
			buttons.add(Button.DPAD_UP);
		if(gamepad1.dpad_right)
			buttons.add(Button.DPAD_RIGHT);
		if(gamepad1.dpad_down)
			buttons.add(Button.DPAD_DOWN);
		if(gamepad1.dpad_left)
			buttons.add(Button.DPAD_LEFT);

		return buttons;
	}

	public ButtonListenerTeleop()
	{
		super();
	}

	@Override
	public void loop()
	{
		super.loop();

		EnumSet<Button> newButtons = readButtons();
		
		if(prevButtons != null)
		{
			for(Button button : Button.values())
			{
				if(prevButtons.contains(button) && !newButtons.contains(button))
				{
					onButtonReleased(button);
				}

				else if(newButtons.contains(button) && !prevButtons.contains(button))
				{

					onButtonPressed(button);

				}
			}
		}
		
		prevButtons = newButtons;
	}

	/**
	 * Add one or more sequence elements to the queue.  They will be run in the order specified, but simultaneously with
	 * the regular teleop code in loop().
	 * @param steps
	 */
	protected void executeSequenceSteps(SequenceStep... steps)
	{
		LinkedList<SequenceStep> stepsList = new LinkedList<SequenceStep>();
		Collections.addAll(stepsList, steps);
		super.spawnThread(stepsList);
	}

	@Override
	public void addSteps(LinkedList<SequenceStep> steps)
	{
		//do nothing
	}

	/**
	 * Called when a button on the controller is pressed.
	 * @param button the button which was pressed
	 */
	public abstract void onButtonPressed(Button button);

	/**
	 * Called when a button on the controller is released.
	 * @param button the button which was released
	 */
	public abstract void onButtonReleased(Button button);
}
