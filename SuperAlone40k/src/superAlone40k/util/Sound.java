package superAlone40k.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;


/**
 * Central sound processing class which offers functionality
 * to play a single background song and several game effects.
 * <br><br>
 * Maybe find some fancy music on https://onlinesequencer.net/
 */
public class Sound {
	public static int PLAYER_JUMP = 1;
	public static int PLAYER_COLLIDE = 2;
	public static int PLAYER_DEATH = 3;
    public static int PLAYER_SCORE = 4;
	
	// Max. of 16 channels is possible!
	private static Map<Integer, MidiChannel> channelById = new HashMap<>();
	
	private static Sequencer musicSequencer;
	private static MidiChannel[] fxChannels;
	
	private final static int MAX_CHANNELS = 16;
	private static int currentChannelIndex = 0;
	
	public static void initializeSound() {
		initializeBackgroundMusic();
		initializeSoundFxChannels();
	}

	public static void playNoteFor(final MidiChannel channel, int note, int velocity) {
		channel.noteOn(note, velocity);
	}
	
	public static void stopNoteFor(final MidiChannel channel, int note, int velocity) {
		channel.noteOff(note, velocity);
	}
	
	/**
	 * Set the volume for the given channel. (Gain between 0 and 1)  
	 */
	public static void setVolumeFor(final MidiChannel channel, final double gain) {
		channel.controlChange(7, (int) (gain * 127));
		// TODO: Seems not to work properly?
	}
	
	public static void setGlobalVolume(final double gain) {
		for(final MidiChannel channel : channelById.values()) {
			setVolumeFor(channel, gain);
		}
		// TODO: Do something similar for music.
	}
	
	/**
	 * Changes the instrument for the given channel.
	 */
	public static void changeInstrumentFor(final MidiChannel channel, final int instrument) {
		channel.programChange(instrument);
	}
	
	public static void setBackgroundTempo(float factor) {
		musicSequencer.setTempoFactor(factor);
	}
	
	/**
	 * Creates an unique {@link MidiChannel} based on the given id.
	 * @param channelId Use Id's defined in {@link Sound}.
	 * @return Returns the sequencer or null if the Id is already used.
	 */
	public static MidiChannel addChannel(int channelId) {
		if(channelById.containsKey(channelId) || currentChannelIndex >= MAX_CHANNELS) {
			return null;
		}
		
		final MidiChannel channel = fxChannels[currentChannelIndex++];
		channelById.put(channelId, channel);
		return channel;
	}
	
	/**
	 * Returns the {@link MidiChannel} mapped by the given ID or null
	 * if the id does not exist. 
	 */
	public static MidiChannel getChannelBy(int channelId) {
		return channelById.get(channelId);
	}
	
	private static void initializeBackgroundMusic() {
		try {
			musicSequencer = MidiSystem.getSequencer();
			musicSequencer.open();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
		
		
		try(InputStream backGroundMusicStream = new BufferedInputStream(new FileInputStream(new File("sound/tetris.mid")))) {
			initializeMusicSequencer(backGroundMusicStream);
		} catch (IOException | InvalidMidiDataException e) {
			try(InputStream classBasedMusicStream = Sound.class.getClassLoader().getResourceAsStream("superAlone40k/tetris.mid")) {
				initializeMusicSequencer(classBasedMusicStream);
			} catch (IOException | InvalidMidiDataException e1) {
				e1.printStackTrace();
			}
//			e.printStackTrace();
		}
	}

	private static void initializeMusicSequencer(InputStream musicStream) throws IOException, InvalidMidiDataException {
		musicSequencer.setSequence(musicStream);
		musicSequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
		musicSequencer.start();
	}
	
	private static void initializeSoundFxChannels() {
		try {
			Synthesizer synthesizer = MidiSystem.getSynthesizer();
			synthesizer.open();
			
			fxChannels = synthesizer.getChannels();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
	}
}