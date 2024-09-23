package com.zqy.sdk.voiceview;

/**
 * 音量类，用于计算音量大小
 */
public final class Volume {

	public static final int MAX_VOLUME = 37;

	/**
	 * 计算音量值
	 * 
	 * @param voiceData
	 * @param bits
	 * @return
	 */
	public static int calculateVolume(byte[] voiceData, int bits) {
		int[] voiceSwing = convertData(voiceData, bits);
		if (voiceSwing == null || voiceSwing.length == 0) {
			return 0;
		}

		// LogUtils.v(TAG, "voiceSwing.length = " + voiceSwing.length);

		float total = 0;
		for (int index = 0; index < voiceSwing.length; index++) {
			total += voiceSwing[index] * voiceSwing[index];
		}
		total = total / voiceSwing.length;
		// LogUtils.v(TAG, "total = " + total);

		float sumTotal = 0;
		for (int index = 0; index < voiceSwing.length; index++) {
			sumTotal += voiceSwing[index];
		}
		sumTotal = sumTotal / voiceSwing.length;
		// LogUtils.v(TAG, "sumTotal = " + sumTotal);

		int SHORT_MAX_VALUE = (int) (Math.pow(2, bits - 1) - 1);
		// final int SHORT_MAX_VALUE = 20000;
		// LogUtils.v(TAG, "SHORT_MAX_VALUE = " + SHORT_MAX_VALUE);

		double energy = (Math.sqrt(total - sumTotal * sumTotal));
		int volume = (int) (MAX_VOLUME * Math.log10(energy * MAX_VOLUME
				* Math.sqrt(2) / SHORT_MAX_VALUE + 1));
		if (volume < 0) {
			volume = 0;
		}
		
		if (volume > MAX_VOLUME) {
			volume = MAX_VOLUME;
		}

		return volume;
	}

	/**
	 * 转化音频数据
	 * 
	 * @param voiceData
	 * @param bits
	 * @return
	 */
	private static int[] convertData(byte[] voiceData, int bits) {
		int[] voiceSwing = null;
		int dataLen = voiceData.length;
		if (bits == 8) {
			voiceSwing = new int[dataLen];
			for (int index = 0; index < dataLen; index++) {
				voiceSwing[index] = voiceData[index];
			}
		} else if (bits == 16) {
			voiceSwing = new int[dataLen / 2];
			for (int index = 0; index < dataLen / 2; index++) {
				byte b0 = voiceData[2 * index];
				byte b1 = voiceData[2 * index + 1];
				voiceSwing[index] = bytes2short(b0, b1);
			}
		}

		return voiceSwing;
	}

	/**
	 * byte转化为short
	 * 
	 * @param b0
	 * @param b1
	 * @return
	 */
	private static short bytes2short(byte b0, byte b1) {
		int i = 0;
		short out = 0;
		if (b0 < 0) {
			i = 256 + b0;
		} else {
			i = b0;
		}
		out += i;
		if (b1 < 0) {
			i = 256 + b1;
		} else {
			i = b1;
		}
		out += (i << 8);

		return out;
	}
}
