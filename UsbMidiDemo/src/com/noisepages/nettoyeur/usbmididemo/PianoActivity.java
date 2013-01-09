/**
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 * Piano GUI based on MyPiano by ZhangFL
 * http://code.google.com/p/mobexamples/wiki/android_mypiano
 * http://www.eoeandroid.com/space.php?uid=1178
 */

package com.noisepages.nettoyeur.usbmididemo;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.noisepages.nettoyeur.midi.MidiReceiver;
import com.noisepages.nettoyeur.usbmidi.PermissionHandler;
import com.noisepages.nettoyeur.usbmidi.UsbMidiDevice;
import com.noisepages.nettoyeur.usbmidi.UsbMidiDevice.UsbMidiInput;
import com.noisepages.nettoyeur.usbmidi.UsbMidiDevice.UsbMidiInterface;
import com.noisepages.nettoyeur.usbmidi.UsbMidiDevice.UsbMidiOutput;

public class PianoActivity extends Activity implements View.OnTouchListener {

	private static final String TAG = "UsbMidiDemo";
	private boolean touchState = false;
	private ImageButton[] keys;
	private final int[] imageUp = new int[] { R.drawable.white1,
			R.drawable.black1, R.drawable.white2, R.drawable.black2,
			R.drawable.white3, R.drawable.white4, R.drawable.black3,
			R.drawable.white5, R.drawable.black4, R.drawable.white6,
			R.drawable.black5, R.drawable.white7, R.drawable.white8 };
	private final int[] imageDown = new int[] { R.drawable.white11,
			R.drawable.black11, R.drawable.white21, R.drawable.black21,
			R.drawable.white31, R.drawable.white41, R.drawable.black31,
			R.drawable.white51, R.drawable.black41, R.drawable.white61,
			R.drawable.black51, R.drawable.white71, R.drawable.white81 };
	private UsbMidiOutput midiOutput;

	private UsbMidiDevice midiDevice = null;
	
	private final MidiReceiver receiver = new MidiReceiver() {
		@Override
		public void onNoteOn(int channel, int key, final int velocity) {
			final int index = key - 60;
			if (index >= 0 && index < 13) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (velocity > 0)
							keyDown(index);
						else
							keyUp(index);
					}
				});
			}
		}

		@Override
		public void onNoteOff(int channel, int key, int velocity) {
			final int index = key - 60;
			if (index >= 0 && index < 13) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						keyUp(index);
					}
				});
			}
		}

		// We won't use the remaining callbacks in this demo.
		@Override
		public void onProgramChange(int channel, int program) {
			toast("program change: " + channel + ", " + program);
		}

		@Override
		public void onPolyAftertouch(int channel, int key, int velocity) {
			toast("aftertouch: " + channel + ", " + key + ", " + velocity);
		}

		@Override
		public void onPitchBend(int channel, int value) {
			toast("pitch bend: " + channel + ", " + value);
		}

		@Override
		public void onControlChange(int channel, int controller, int value) {
			toast("control change: " + channel + ", " + controller + ", " + value);
		}

		@Override
		public void onAftertouch(int channel, int velocity) {
			toast("aftertouch: " + channel + ", " + velocity);
		}

		@Override
		public void onRawByte(int value) {
			toast("raw byte: " + value);
		}
	};

	private Toast toast = null;
	
	private void toast(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (toast == null) {
					toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
				}
				toast.setText(TAG + ": " + msg);
				toast.show();
			}
		});
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.main);
		ImageButton white1 = (ImageButton) findViewById(R.id.white1);
		ImageButton white2 = (ImageButton) findViewById(R.id.white2);
		ImageButton white3 = (ImageButton) findViewById(R.id.white3);
		ImageButton white4 = (ImageButton) findViewById(R.id.white4);
		ImageButton white5 = (ImageButton) findViewById(R.id.white5);
		ImageButton white6 = (ImageButton) findViewById(R.id.white6);
		ImageButton white7 = (ImageButton) findViewById(R.id.white7);
		ImageButton white8 = (ImageButton) findViewById(R.id.white8);
		ImageButton black1 = (ImageButton) findViewById(R.id.black1);
		ImageButton black2 = (ImageButton) findViewById(R.id.black2);
		ImageButton black3 = (ImageButton) findViewById(R.id.black3);
		ImageButton black4 = (ImageButton) findViewById(R.id.black4);
		ImageButton black5 = (ImageButton) findViewById(R.id.black5);
		white1.setTag(0);
		white2.setTag(2);
		white3.setTag(4);
		white4.setTag(5);
		white5.setTag(7);
		white6.setTag(9);
		white7.setTag(11);
		white8.setTag(12);
		black1.setTag(1);
		black2.setTag(3);
		black3.setTag(6);
		black4.setTag(8);
		black5.setTag(10);
		keys = new ImageButton[] { white1, black1, white2, black2, white3,
				white4, black3, white5, black4, white6, black5, white7, white8 };
		for (ImageButton key : keys) {
			key.setOnTouchListener(this);
		}
		
		UsbMidiDevice.installPermissionHandler(this, new PermissionHandler() {

			@Override
			public void onPermissionGranted() {
				midiDevice.open(PianoActivity.this);
				List<UsbMidiInput> inputs = midiDevice.getInterfaces().get(0).getInputs();
				if (!inputs.isEmpty()) {
					UsbMidiInput input = inputs.get(0);
					input.setReceiver(receiver);
					input.start();
				}
				List<UsbMidiOutput> outputs = midiDevice.getInterfaces().get(0).getOutputs();
				if (!outputs.isEmpty()) {
					midiOutput = outputs.get(0);
				}
			}

			@Override
			public void onPermissionDenied() {
				toast(TAG + ": permission denied for device " + midiDevice);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		UsbMidiDevice.uninstallPermissionHandler(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		for (UsbMidiDevice device : UsbMidiDevice.getMidiDevices(this)) {
			for (UsbMidiInterface iface : device.getInterfaces()) {
				if (!iface.getInputs().isEmpty()) {
					midiDevice = device;
					toast("USB MIDI devices\n\n" + device.toString());
					midiDevice.requestPermission(this);
					return;
				}
			}
		}
		toast("No USB MIDI devices found");
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (midiDevice != null) {
			midiDevice.close();
		}
	}
	
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (midiOutput == null) return false;
		if (!(view instanceof ImageButton)) return false;
		ImageButton key = (ImageButton) view;
		Object tag = key.getTag();
		if (tag == null || !(tag instanceof Integer))
			return false;
		int index = (Integer) tag;
		int action = motionEvent.getAction();
		if (action == MotionEvent.ACTION_DOWN && !touchState) {
			touchState = true;
			midiOutput.onNoteOn(0, index + 60, 100);
			keyDown(index);
		} else if (action == MotionEvent.ACTION_UP && touchState) {
			touchState = false;
			midiOutput.onNoteOff(0, index + 60, 64);
			keyUp(index);
		}
		return true;
	}

	private void keyDown(int n) {
		keys[n].setImageResource(imageDown[n]);
	}

	private void keyUp(int n) {
		keys[n].setImageResource(imageUp[n]);
	}
}