package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JPanel;

import pdc.PDC;
import pdc.PDCI;

public class PDCCanvas extends JPanel implements MouseListener {
	
	private static final Dimension CANVAS_SIZE = new Dimension(180, 180);
	
	private JPDCGUI gui;
	private PDCI image;
	private PDC currentCommand;
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		
		// Background
		// TODO Make more useful color - checkerboard?
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, CANVAS_SIZE.width, CANVAS_SIZE.height);
		
		// Draw commands so far
		ArrayList<PDC> commandList = image.getCommandList();
		
		System.out.println("Drawing " + commandList.size() + " commands");
		
		for(PDC command : commandList) {
			// Set the stroke width and fill color
			g2d.setStroke(new BasicStroke(command.getStrokeWidth()));
			
			if(command.getType() == PDC.TYPE_PATH) {
				// Get all points
				ArrayList<Point> points = command.getPointArray();
				
				// Draw filled?
				if(command.getPathOpenRadius() == PDC.PATH_CLOSED) {
					// Make array of x and y
					int[] xArr = new int[points.size()];
					int[] yArr = new int[points.size()];
				    for(int i = 0; i < points.size(); i++) {
				        xArr[i] = points.get(i).x;
				        yArr[i] = points.get(i).y;
				    }
				    g2d.setColor(command.getFillColor());
					g2d.drawPolygon(xArr, yArr, points.size());
				}
				
				// Draw lines of outline
				g2d.setColor(command.getStrokeColor());
				if(command.getStrokeWidth() > 0 && command.getNumberOfPoints() > 1) {
					for(int i = 1; i < points.size(); i++) {
						Point last = points.get(i - 1);
						Point next = points.get(i);
						g2d.drawLine(last.x, last.y, next.x, next.y);
					}
				}
			} else {
				// Circle
				int radius = command.getPathOpenRadius();
				Point center = command.getPointArray().get(0);

				g2d.setColor(command.getFillColor());
				g2d.fillOval(center.x - (radius / 2), center.y - (radius / 2), radius, radius);
			}
		}
	}
	
	public PDCCanvas(JPDCGUI gui) {
		this.gui = gui;
		setPreferredSize(CANVAS_SIZE);
		addMouseListener(this);
		
		image = new PDCI(getSize());
		
		repaint();
	}
	
	public void reset() {
		image = new PDCI(getSize());
		repaint();
		System.out.println("Canvas reset");
	}
	
	public void save(String path) {
		try {
			System.out.println("Saving...");
			image.writeToFile(path);
			System.out.println("Saved to " + path);
		} catch (Exception e) {
			System.err.println("Error writing file to " + path + ": " + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	public void removeLastCommand() {
		System.out.println("Removing last command...");
		image.removeLastCommand();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// Add point
		if(currentCommand == null) {
			// Begin command
			currentCommand = new PDC(gui.getType(), PDC.NOT_HIDDEN, gui.getStrokeColor(), gui.getStrokeWidth(), gui.getFillColor(), gui.getOpenPathRadius());
			System.out.println("Began new command");
		}
		
		// Add another point on path, or circle without a radius
		if((gui.getType() == PDC.TYPE_PATH) 
		|| (gui.getType() == PDC.TYPE_CIRCLE && currentCommand.getNumberOfPoints() < 1)) {
			Point p = e.getPoint();
			System.out.println("Added point " + p.toString());
			
			currentCommand.addPoint(p);
		} else {
			System.out.println("Circle can only have one radius");
		}
		
		repaint();
	}

	
	@Override
	public void mouseExited(MouseEvent e) {
		if(currentCommand != null) {
			// Stop the path, add the command
			image.addCommand(currentCommand);
			currentCommand = null;
			
			System.out.println("Mouse exited canvas, finalizing current command");
		}
	}
	
	@Override
	public void mouseEntered(MouseEvent e) { }
	public void mousePressed(MouseEvent e) { }
	public void mouseReleased(MouseEvent e) { }


}