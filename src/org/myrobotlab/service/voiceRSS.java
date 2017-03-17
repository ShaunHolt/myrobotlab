/**
 *                    
 * @author steve (at) myrobotlab.org
 *  
 * This file is part of MyRobotLab (http://myrobotlab.org).
 *
 * MyRobotLab is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version (subject to the "Classpath" exception
 * as provided in the LICENSE.txt file that accompanied this code).
 *
 * MyRobotLab is distributed in the hope that it will be useful or fun,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * All libraries in thirdParty bundle are subject to their own license
 * requirements - please refer to http://myrobotlab.org/libraries for 
 * details.
 * 
 * Enjoy !
 * 
 * */
package org.myrobotlab.service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.myrobotlab.framework.Service;
import org.myrobotlab.framework.ServiceType;
import org.myrobotlab.io.FileIO;
import org.myrobotlab.logging.Level;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.logging.Logging;
import org.myrobotlab.logging.LoggingFactory;
import org.myrobotlab.service.data.AudioData;
import org.myrobotlab.service.interfaces.AudioListener;
import org.myrobotlab.service.interfaces.SpeechRecognizer;
import org.myrobotlab.service.interfaces.SpeechSynthesis;
import org.myrobotlab.service.interfaces.TextListener;
import org.slf4j.Logger;

import java.io.FileOutputStream;
import com.voicerss.tts.AudioCodec;
import com.voicerss.tts.AudioFormat;
//import com.voicerss.tts.Languages;
//import com.voicerss.tts.SpeechDataEvent;
//import com.voicerss.tts.SpeechDataEventListener;
//import com.voicerss.tts.SpeechErrorEvent;
//import com.voicerss.tts.SpeechErrorEventListener;
import com.voicerss.tts.VoiceParameters;
import com.voicerss.tts.VoiceProvider;




	

public class voiceRSS extends Service implements TextListener,SpeechSynthesis,  AudioListener {

	  transient public final static Logger log = LoggerFactory.getLogger(voiceRSS.class);
	  private static final long serialVersionUID = 1L;
	  // default voice
	  public String voice = "fr-fr";
	  public HashSet<String> voices = new HashSet<String>();
	  public String key = "0000";
	  // this is a peer service.
	  transient AudioFile audioFile = null;
	  // TODO: fix the volume control
	  // private float volume = 1.0f;
	  
	  transient CloseableHttpClient client;

	  public voiceRSS(String n) {
		    super(n);

		    // TODO: be country/language aware when asking for voices?
		    // maybe have a get voices by language/locale
		    // Arabic
		    voices.add("ca-es");//Catalan
		voices.add("zh-cn");//Chinese (China)
voices.add("zh-hk");//Chinese (Hong Kong)
voices.add("zh-tw");//Chinese (Taiwan)
voices.add("da-dk");//Danish
voices.add("nl-nl");//Dutch
voices.add("en-au");//English (Australia)
voices.add("en-ca");//English (Canada)
voices.add("en-gb");//English (Great Britain)
voices.add("en-in");//English (India)
voices.add("en-us");//English (United States)
voices.add("fi-fi");//Finnish
voices.add("fr-ca");//French (Canada)
voices.add("fr-fr");//French (France)
voices.add("de-de");//German
voices.add("it-it");//Italian
voices.add("ja-jp");//Japanese
voices.add("ko-kr");//Korean
voices.add("nb-no");//Norwegian
voices.add("pl-pl");//Polish
voices.add("pt-br");//Portuguese (Brazil)
voices.add("pt-pt");//Portuguese (Portugal)
voices.add("ru-ru");//Russian
voices.add("es-mx");//Spanish (Mexico)
voices.add("es-es");//Spanish (Spain)
voices.add("sv-se");//Swedish (Sweden)
		
		  }
	  
public void startService() {
		    super.startService();
		    if (client == null) {
		        // new MultiThreadedHttpConnectionManager()
		        client = HttpClients.createDefault();
		    }
		    audioFile = (AudioFile) startPeer("audioFile");
		    audioFile.startService();
		    subscribe(audioFile.getName(), "publishAudioStart");
		    subscribe(audioFile.getName(), "publishAudioEnd");
		    // attach a listener when the audio file ends playing.
		    audioFile.addListener("finishedPlaying", this.getName(), "publishEndSpeaking");
		  }

 public AudioFile getAudioFile() {
		    return audioFile;
		  }

 @Override
public ArrayList<String> getVoices() {
		    return new ArrayList<String>(voices);
		  }

 @Override
 public String getVoice() {
   return voice;
 }
 
 @Override
 public boolean setVoice(String voice) {
   this.voice = voice;
   return voices.contains(voice);
 }

 public String getKey(){
	 return key;
 }
 
 public void setKey(String key){
	 this.key = key;
 }
 
 @Override
 public void setLanguage(String l) {
   // FIXME ! "MyLanguages", "sonid8" ???
   // FIXME - implement !!!
 }

 public String getMp3Url(String toSpeak) {
   HttpPost post = null;
   try {
	   String url = "";
	   post = new HttpPost(url); 
	   HttpResponse response = client.execute(post);
	      log.info(response.getStatusLine().toString());
	      HttpEntity entity = response.getEntity();
	      byte[] b = FileIO.toByteArray(entity.getContent());
	      // parse out mp3 file url
	      String mp3Url = null;
	      String data = new String(b);
	      String startTag = "var myPhpVar = '";
	      int startPos = data.indexOf(startTag);
	      if (startPos != -1) {
	        int endPos = data.indexOf("';", startPos);
	        if (endPos != -1) {
	          mp3Url = data.substring(startPos + startTag.length(), endPos);
	        }
	      }
	      if (mp3Url == null) {
	        error("could not get mp3 back from voicerss server !");
	      }
	      return mp3Url;
	      
   }catch (Exception e) {
	      Logging.logError(e);
	    }
   		
   return null;

 }

 public byte[] getRemoteFile(String toSpeak) {

   String mp3Url = getMp3Url(toSpeak);
   HttpGet get = null;
   byte[] b = null;
   try {
     HttpResponse response = null;
     // fetch file
     get = new HttpGet(mp3Url);
     log.info("mp3Url {}", mp3Url);
     // get mp3 file & save to cache
     response = client.execute(get);
     log.info("got {}", response.getStatusLine());
     HttpEntity entity = response.getEntity();
     // cache the mp3 content
     b = FileIO.toByteArray(entity.getContent());
     EntityUtils.consume(entity);
   } catch (Exception e) {
     Logging.logError(e);
   } finally {
     if (get != null) {
       get.releaseConnection();
     }
   }

   return b;
 }

 @Override
 public boolean speakBlocking(String toSpeak) throws Exception {
  
	 
	    log.info("speak {}", toSpeak);
	    if (voice == null) {
	        log.warn("voice is null! setting to default: fr-fr");
	        voice = "fr-fr";
	      }
	    
	    String filename = this.getLocalFileName(this, toSpeak, "mp3");	    
	    String filenametts = "audioFile"+ File.separator + filename;
	     VoiceProvider tts = new VoiceProvider(getKey());
			
	     VoiceParameters params = new VoiceParameters(toSpeak,getVoice() ); //Languages.English_UnitedStates
	     params.setCodec(AudioCodec.WAV);
	     params.setFormat(AudioFormat.Format_44KHZ.AF_44khz_16bit_stereo);
	     params.setBase64(false);
	     params.setSSML(false);
	     params.setRate(0);
			
	     if (audioFile.cacheContains(filename)) {
	    	   invoke("publishStartSpeaking", toSpeak);
	    	   audioFile.playBlocking(filenametts);
	    	   invoke("publishEndSpeaking", toSpeak);
	    	   log.info("Finished waiting for completion.");
	    	   return false;
		  
	     }
	     

	     
	     byte[] voix = tts.speech(params);
		 String filenametts2 = "audioFile"+ File.separator + filename;
		
		 
		 String dossier =this.getLocalDirectory(this);	 
		 File f = new File (dossier);
		 if (f.exists()){}else{
			 File fb = new File(dossier); 
			 fb.mkdirs(); 
		 }
		 

	     FileOutputStream fos = new FileOutputStream(filenametts2);
	     fos.write(voix, 0, voix.length);
	     fos.flush();
	     fos.close();	 
	     
	     invoke("publishStartSpeaking", toSpeak);
	     audioFile.playBlocking(filenametts2);
	     invoke("publishEndSpeaking", toSpeak);
	     log.info("Finished waiting for completion.");
	     return false;
	 

 }

 @Override
 public void setVolume(float volume) {
   // TODO: fix the volume control
   log.warn("Volume control not implemented in VoiceRSS.");
 }

 @Override
 public float getVolume() {
   return 0;
 }

 @Override
 public void interrupt() {
   // TODO: Implement me!
 }

 @Override
 public void onText(String text) {
   log.info("ON Text Called: {}", text);
   try {
     speak(text);
   } catch (Exception e) {
     Logging.logError(e);
   }
 }

 @Override
 public String getLanguage() {
   return null;
 }

 // HashSet<String> audioFiles = new HashSet<String>();
 Stack<String> audioFiles = new Stack<String>();


 
 
 public AudioData speak(String toSpeak) throws Exception {
	    // this will flip to true on the audio file end playing.
	    AudioData ret = null;
	    log.info("speak {}", toSpeak);
	    if (voice == null) {
	        log.warn("voice is null! setting to default: fr-fr");
	        voice = "fr-fr";
	      }
	    
	    String filename = this.getLocalFileName(this, toSpeak, "mp3");	    
	    
	     VoiceProvider tts = new VoiceProvider(getKey());
			
	     VoiceParameters params = new VoiceParameters(toSpeak,getVoice() ); //Languages.English_UnitedStates
	     params.setCodec(AudioCodec.WAV);
	     params.setFormat(AudioFormat.Format_44KHZ.AF_44khz_16bit_stereo);
	     params.setBase64(false);
	     params.setSSML(false);
	     params.setRate(0);
			
	     if (audioFile.cacheContains(filename)) {
		      ret = audioFile.playCachedFile(filename);
		      utterances.put(ret, toSpeak);
		      
		      return ret;
		      
		  
	     }
	     

	     
	     byte[] voix = tts.speech(params);
		 String filenametts = "audioFile"+ File.separator + filename;
		
		 
		 String dossier =this.getLocalDirectory(this);	 
		 File f = new File (dossier);
		 if (f.exists()){}else{
			 File fb = new File(dossier); 
			 fb.mkdirs(); 
		 }
		 

	     FileOutputStream fos = new FileOutputStream(filenametts);
	     fos.write(voix, 0, voix.length);
	     fos.flush();
	     fos.close();	 
	     
	     ret = audioFile.playCachedFile(filename);
	     utterances.put(ret, toSpeak);
	     
	     return ret;
	    
	    


	  }

 public String getLocalDirectory(SpeechSynthesis provider) throws UnsupportedEncodingException {
   // TODO: make this a base class sort of thing.
	 
	    return  "audioFile"+ File.separator + provider.getClass().getSimpleName() + File.separator + URLEncoder.encode(provider.getVoice(), "UTF-8") ;

 }

 @Override
 public String getLocalFileName(SpeechSynthesis provider, String toSpeak, String audioFileType) throws UnsupportedEncodingException {
   // TODO: make this a base class sort of thing.
	 
	    return provider.getClass().getSimpleName() + File.separator + URLEncoder.encode(provider.getVoice(), "UTF-8") + File.separator + DigestUtils.md5Hex(toSpeak) + "."
        + audioFileType;

 }

 @Override
 public void addEar(SpeechRecognizer ear) {
   // TODO: move this to a base class. it's basically the same for all
   // mouths/ speech synth stuff.
   // when we add the ear, we need to listen for request confirmation
   addListener("publishStartSpeaking", ear.getName(), "onStartSpeaking");
   addListener("publishEndSpeaking", ear.getName(), "onEndSpeaking");
 }

 public void onRequestConfirmation(String text) {
   try {
     speakBlocking(String.format("did you say. %s", text));
   } catch (Exception e) {
     Logging.logError(e);
   }
 }

 @Override
 public List<String> getLanguages() {
   // TODO Auto-generated method stub
   ArrayList<String> ret = new ArrayList<String>();
   // FIXME - add iso language codes currently supported e.g. en en_gb de
   // etc..
   return ret;
 }

 // audioData to utterance map TODO: revisit the design of this
 HashMap<AudioData, String> utterances = new HashMap<AudioData, String>();

 @Override
 public String publishStartSpeaking(String utterance) {
   log.info("publishStartSpeaking {}", utterance);
   return utterance;
 }

 @Override
 public String publishEndSpeaking(String utterance) {
   log.info("publishEndSpeaking {}", utterance);
   return utterance;
 }

 @Override
 public void onAudioStart(AudioData data) {
   log.info("onAudioStart {} {}", getName(), data.toString());
   // filters on only our speech
   if (utterances.containsKey(data)) {
     String utterance = utterances.get(data);
     invoke("publishStartSpeaking", utterance);
   }
 }

 @Override
 public void onAudioEnd(AudioData data) {
   log.info("onAudioEnd {} {}", getName(), data.toString());
   // filters on only our speech
   if (utterances.containsKey(data)) {
     String utterance = utterances.get(data);
     invoke("publishEndSpeaking", utterance);
     utterances.remove(data);
   }
 }

 // TODO Add a function to compare (is there the word "fruit" in the banana's
// document ? )

public static void main(String[] args) {
   LoggingFactory.init(Level.INFO);
   try {
    
     voiceRSS speech = (voiceRSS) Runtime.start("voiceRSS", "voiceRSS");
     
     // TODO: fix the volume control
     // speech.setVolume(0);
     speech.speakBlocking("does this work");
     speech.speakBlocking("uh oh");
     speech.speakBlocking("to be or not to be that is the question, weather tis nobler in the mind to suffer the slings and arrows of ");
     speech.speakBlocking("I'm afraid I can't do that.");

     // speech.speak("this is a test");
     //
     // speech.speak("i am saying something new once again again");
     // speech.speak("one");
     // speech.speak("two");
     // speech.speak("three");
     // speech.speak("four");
     /*
      * speech.speak("what is going on"); //speech.speakBlocking(
      * speech.speak("hello world"); speech.speak("one two three four");
      */
     // arduino.setBoard(Arduino.BOARD_TYPE_ATMEGA2560);
     // arduino.connect(port);
     // arduino.broadcastState();
   } catch (Exception e) {
     Logging.logError(e);
   }
 }
 /**
  * This static method returns all the details of the class without it having
  * to be constructed. It has description, categories, dependencies, and peer
  * definitions.
  * 
  * @return ServiceType - returns all the data
  * 
  */
 static public ServiceType getMetaData() {
   ServiceType meta = new ServiceType(voiceRSS.class.getCanonicalName());
   meta.addDescription("VoiceRSS speech synthesis service.");
   meta.addCategory("speech");
   meta.setSponsor("Steve");
   meta.addPeer("audioFile", "AudioFile", "audioFile");
   meta.addTodo("test speak blocking - also what is the return type and AudioFile audio track id ?");
   meta.addDependency("org.apache.commons.httpclient", "4.5.2");
   meta.addDependency("org.voicerss.tts","1.0");
   return meta;
 }


}
