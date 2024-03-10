import numpy as np
import wave
import torch
from scipy.io import wavfile




def load_wav(filename ):
    #wavファイルを読み込む
    sample_rate, audio = wavfile.read(filename)
    print('Loaded wav file at', filename)
    print('Sample rate:', sample_rate)
    audio = audio / 32767
    print(audio)
    return audio

    

def save_tensor_to_wav(tensor, filename, sample_rate=16000):
    
    #サンプリングレートを指定して音声ファイルを保存する
    np_audio = np.array(tensor)
    np_audio = np_audio * 32767
    np_audio = np.int16(np_audio)
    
    with wave.open(filename, 'w') as file:
        file.setnchannels(1)
        file.setsampwidth(2)
        file.setframerate(sample_rate)
        file.writeframes(np_audio.tobytes())
    print('Saved wav file at', filename)