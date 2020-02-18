package com.birlasoft.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class CommonUtils {
	public static BufferedImage resize(BufferedImage image, int width, int height) {
		BufferedImage resizeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D grap = resizeImage.createGraphics();
		grap.drawImage(image, 0, 0, width, height, null);
		grap.dispose();
		return resizeImage;
	}
}