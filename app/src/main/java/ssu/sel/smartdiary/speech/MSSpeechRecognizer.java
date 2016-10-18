package ssu.sel.smartdiary.speech;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.bing.speech.SpeechClientStatus;
import com.microsoft.projectoxford.speechrecognition.DataRecognitionClient;
import com.microsoft.projectoxford.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.projectoxford.speechrecognition.RecognitionResult;
import com.microsoft.projectoxford.speechrecognition.RecognitionStatus;
import com.microsoft.projectoxford.speechrecognition.SpeechRecognitionMode;
import com.microsoft.projectoxford.speechrecognition.SpeechRecognitionServiceFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by hanter on 2016. 10. 15..
 */
public class MSSpeechRecognizer {
    private final static String PRIMARY_KEY = "af104e9ebb5f47a195751fb24680805a";
    private final static String SECONDARY_KEY = "3196a12b1ce94fa1b37369ea991e4f9c";

    private Activity activity;
    private ISpeechRecognitionServerEvents recogEvents;

    private OnRecognizeDoneListener listener = null;
    private SpeechResponseStatus speechResponseStatus
            = SpeechResponseStatus.NotReceived;

    private int recordedCount = 0;
    private int recognizingRecordCount;

    private DataRecognitionClient dataClient = null;

    public interface OnRecognizeDoneListener {
        void onRecognizeDone();
        void onPartialRecognizeDone();
        void onPartialRecognize(String text);
        void onFail(String message);
    }
    public enum SpeechResponseStatus { NotReceived, OK, Failed, Timeout }


    //waitsecnods 200
    public MSSpeechRecognizer(Activity activity, OnRecognizeDoneListener l) {
        this.activity = activity;
        this.listener = l;

        recogEvents = new ISpeechRecognitionServerEvents() {
            @Override
            public void onPartialResponseReceived(final String s) {
                listener.onPartialRecognize(s);
            }

            @Override
            public void onFinalResponseReceived(final RecognitionResult response) {
                boolean isFinalDictationMessage =
                        (response.RecognitionStatus == RecognitionStatus.EndOfDictation ||
                                response.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout);

                boolean isFailed = response.RecognitionStatus == RecognitionStatus.InitialSilenceTimeout ||
                        response.RecognitionStatus == RecognitionStatus.BabbleTimeout ||
                        response.RecognitionStatus == RecognitionStatus.RecognitionError ||
                        response.RecognitionStatus == RecognitionStatus.None;

                Log.d("MS Speech Recognizer", "Final Response: " + response.RecognitionStatus);

                //error
                if (isFailed) {
                    listener.onFail("Final Response Error: " + response.RecognitionStatus.toString());
                    speechResponseStatus = SpeechResponseStatus.Failed;
                } else {
                    if (isFinalDictationMessage) {
                        if (recognizingRecordCount >= recordedCount) {
                            listener.onRecognizeDone();
                        } else {
                            listener.onPartialRecognizeDone();
                            recognizingRecordCount++;
                            transmitAudio(recognizingRecordCount);
                        }

                        speechResponseStatus = SpeechResponseStatus.OK;
                    } else {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < response.Results.length; i++) {
                            sb.append(response.Results[i].DisplayText);
                        }
                        listener.onPartialRecognize(sb.toString());
                    }
                }
            }

            @Override
            public void onIntentReceived(String s) {}

            @Override
            public void onError(final int errorCode, final String response) {
                listener.onFail("ErrorCode: " + errorCode
                        + "\nStatus: "
                        + SpeechClientStatus.fromInt(errorCode)
                        + "\nResponse Message: " + response);
            }

            @Override
            public void onAudioEvent(boolean recording) {}
        };
    }

    public void startRecognize() {
        dataClient = SpeechRecognitionServiceFactory.createDataClient(
                activity, SpeechRecognitionMode.LongDictation,
                "en-us", recogEvents, PRIMARY_KEY, SECONDARY_KEY);

        recordedCount = WavRecorder.getRecordedFileNum();
        recognizingRecordCount = 1;
        transmitAudio(recognizingRecordCount);
    }

    private void transmitAudio(int recordNum) {
        Log.d("MS Speech Recognizer", "Converting the record #" + recordNum);
        RecognitionTask doDataReco = new RecognitionTask(dataClient,
                SpeechRecognitionMode.LongDictation,
                WavRecorder.getRecordedFile(recordNum));
        try {
            doDataReco.execute().get(300, TimeUnit.SECONDS);
        } catch (Exception e) {
            doDataReco.cancel(true);
            speechResponseStatus = SpeechResponseStatus.Timeout;
        }
    }

    private class RecognitionTask extends AsyncTask<Void, Void, Void> {
        DataRecognitionClient dataClient;
        SpeechRecognitionMode mode;
        File recordfile;

        RecognitionTask(DataRecognitionClient dataClient,
                        SpeechRecognitionMode mode,
                        File recordfile) {
            this.dataClient = dataClient;
            this.mode = mode;
            this.recordfile = recordfile;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                InputStream fileStream
                        = new FileInputStream(WavRecorder.getRecordedFile(recognizingRecordCount));
                int bytesRead = 0;
                byte[] buffer = new byte[16384];

                do {
                    bytesRead = fileStream.read(buffer);
                    if (bytesRead > -1) {
                        dataClient.sendAudio(buffer, bytesRead);
                    }
                } while (bytesRead > 0);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            finally {
                dataClient.endAudio();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("MS Speech Recognizer", "send recognition request");
        }
    }
}
