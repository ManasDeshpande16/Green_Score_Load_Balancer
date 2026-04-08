package cloudsim.ext.gui.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;

import javax.swing.JPanel;

/**
 * A simple area graph painted on a {@link JPanel}.
 *
 * @author Bhathiya Wickremasinghe
 *
 */
public class SimpleGraph extends JPanel {

	private final Dimension GRAPH_DIM = new Dimension(300, 100);
	private final Dimension PANEL_DIM = new Dimension(330, 110);
	private int colWidth;
	private double maxHeight;
	private double maxSize;
	private Font dataLabelFont;
	private Color graphColor;
	private Color graphColorLight;
	private long[] dataValues;
	private String[] dataLabels;
	private long[] dataValueLabels;
	private String[] axisLabels;
	private BufferedImage graph;

	private int xMargin = 30;
	private int yMargin = 15;
	private Font axisLabelFont;
	private FontMetrics dataLabelFontMetrics;

	/** Constructor with default blue color. */
	public SimpleGraph(long[] dataValues,
					   String[] dataLabels,
					   String[] axisNames,
					   double overallMax){
		this(dataValues, dataLabels, axisNames, overallMax, new Color(50, 50, 140));
	}

	/** Constructor with custom color. */
	public SimpleGraph(long[] dataValues,
					   String[] dataLabels,
					   String[] axisNames,
					   double overallMax,
					   Color color){
		this.dataValues = dataValues;
		this.dataLabels = dataLabels;
		this.axisLabels = axisNames;

		this.setPreferredSize(PANEL_DIM);
		this.setMinimumSize(PANEL_DIM);
		this.setMaximumSize(PANEL_DIM);

		colWidth = (int) (GRAPH_DIM.getWidth() - 20) / 24;
		maxHeight = (GRAPH_DIM.getHeight() - 20);
		if (overallMax > 0){
			maxSize = overallMax;
		} else {
			long localMax = -1;
			for (long val : dataValues){
				if (val > localMax){
					localMax = val;
				}
			}

			maxSize = localMax;
		}

		Font currfont = this.getFont();
		dataLabelFont = new Font(currfont.getName(), Font.PLAIN, 8);
		axisLabelFont = new Font(currfont.getName(), Font.BOLD, 9);

		dataLabelFontMetrics = this.getFontMetrics(dataLabelFont);
		graphColor = color;
		graphColorLight = new Color(
			Math.min(255, color.getRed() + 80),
			Math.min(255, color.getGreen() + 80),
			Math.min(255, color.getBlue() + 80),
			120
		);

		prepareDataValueLabels();
		prepareGraph();
	}

	private void prepareDataValueLabels(){
		double currMax = maxSize / 10;
		int orderOfMax = 0;
		while (currMax >= 1){
			orderOfMax++;
			currMax /= 10;
		}

		String maxStr = Double.toString(maxSize);
		String first = maxStr.substring(0,1);
		int firstDigit = Integer.parseInt(first);

		if (firstDigit == 1){
			int labelCount;
			if (maxStr.length() > 1){
				try {
					labelCount = Integer.parseInt(maxStr.substring(0,2)) / 2;
				} catch (NumberFormatException e){
					labelCount = 5;
				}
			} else {
				labelCount = 5;
			}
			dataValueLabels = new long[labelCount];
			for (int i = 0; i < labelCount; i++){
				dataValueLabels[i] = (long) ((i + 1) * 2  * Math.pow(10, (orderOfMax - 1)));
			}
		} else {
			dataValueLabels = new long[firstDigit];
			for (int i = 0; i < firstDigit; i++){
				dataValueLabels[i] = (long) ((i + 1) * Math.pow(10, orderOfMax));
			}
		}
	}

	private void prepareGraph(){
		GeneralPath graphShape = new GeneralPath();
		graph = new BufferedImage(PANEL_DIM.width, PANEL_DIM.height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2 = graph.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Fill with solid white background so graphs are readable in PDF and on dark UI
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, PANEL_DIM.width, PANEL_DIM.height);

		g2.setColor(Color.black);
		g2.setFont(dataLabelFont);

		g2.translate(xMargin, yMargin);

		// Draw subtle grid lines
		g2.setColor(new Color(220, 220, 220));
		if (dataValueLabels != null) {
			for (long val : dataValueLabels) {
				int gy = (int)(maxHeight - val * maxHeight / maxSize);
				g2.drawLine(0, gy, (int) colWidth * dataLabels.length, gy);
			}
		}

		// Draw axes
		g2.setColor(new Color(80, 80, 80));
		g2.drawLine(0, (int) maxHeight, 0, 0);
		g2.drawLine(0, (int) maxHeight, (int) colWidth * dataLabels.length, (int) maxHeight);

		int x = 0;
		int x1;

		// Draw X axis data labels
		g2.setColor(Color.black);
		if (dataLabels != null){
			for (int i = 0; i <  dataLabels.length; i++){
				x1 = (int) (x);
				g2.drawLine(x, (int) maxHeight + 2, x, (int) maxHeight);
				g2.drawString("" + i, x1, (int) (maxHeight + 10));

				x += colWidth;
			}
		}

		// Draw axis names
		if ((axisLabels != null) && (axisLabels.length == 2)){
			g2.setFont(axisLabelFont);
			g2.drawString(axisLabels[1], (int) (x + (colWidth / 2)), (int) (maxHeight + 10));
			g2.drawString(axisLabels[0], 0, -5);
		}

		// Draw Y axis data labels
		if (dataValueLabels != null){
			g2.setFont(dataLabelFont);
			g2.setColor(new Color(80, 80, 80));

			int y;
			long val;
			String lbl;
			for (int i = 0; i < dataValueLabels.length; i++){
				val = dataValueLabels[i];
				if (val >= 1000000){
					lbl = NumberFormat.getInstance().format(val / 1000000) + "M";
				} else {
					lbl = (NumberFormat.getInstance().format(val));
				}
				x = - dataLabelFontMetrics.stringWidth(lbl);
				y = (int)((maxHeight - val * maxHeight / maxSize)
					+ (dataLabelFontMetrics.getMaxAscent() / 2));
				g2.drawString(lbl, x, y);
			}
		}

		x = 0;
		double y = 0;
		double colHeight;
		graphShape.moveTo(0, maxHeight);
		boolean first = true;

		// Draw graph body
		for (int i = 0; i <  dataValues.length; i++){
			colHeight = ((dataValues[i] / maxSize) * maxHeight);

			if ((colHeight == 0) && (dataValues[i] != 0)){
				colHeight = 1;
			}
			y = (maxHeight - colHeight);

			if (first){
				graphShape.lineTo(x, y);
				x += (colWidth / 2);
				first = false;
			}

			graphShape.lineTo(x, y);
			x += colWidth;
		}

		x -= (colWidth / 2);
		graphShape.lineTo(x, y);
		graphShape.lineTo(x, maxHeight);
		graphShape.closePath();

		// Use gradient fill
		GradientPaint gradient = new GradientPaint(
			0, 0, graphColor,
			0, (int) maxHeight, graphColorLight
		);
		g2.setPaint(gradient);
		g2.fill(graphShape);

		// Draw outline
		g2.setColor(graphColor.darker());
		g2.draw(graphShape);

		g2.dispose();
	}


	public void paint(Graphics g){
		super.paint(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.drawImage(graph, 0, 0, PANEL_DIM.width, PANEL_DIM.height, null);
	}

	/**
	 * @return the graph
	 */
	public BufferedImage getGraphImage() {
		return graph;
	}


}
