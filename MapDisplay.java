import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MapDisplay {

	private JFrame frame;
	
	private Image img;
	
	private boolean navigating=false;
	
	private int ix=0, iy=0, mx=0, my=0;
	private int zoom=0, zoomDiv=10;
	private final int zoomMin=-zoomDiv/2, zoomMax=zoomDiv*5;
	
	private int[] screensize={(int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()};
	
	public MapDisplay() {
		frame = new JFrame();
		frame.setSize(screensize[0], screensize[1]);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		int width=screensize[0], height=screensize[1]-100;
		
		img=Toolkit.getDefaultToolkit().getImage("resources/mapDark.png");
		img=img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		
		JPanel canvas = new JPanel() {
			public void paint(Graphics g) {
				if (img==null) return; //if image has not loaded yet, don't draw
				
				//bring image back to screen if offscreen
//				if (ix>=width)
//					ix-=width+zoom*width/zoomDiv;
//				else if (ix+width+zoom*width/zoomDiv<=0)
//					ix+=width+zoom*width/zoomDiv;
//				
//				if (iy>=height)
//					iy-=height+zoom*height/zoomDiv;
//				else if (iy+height+zoom*height/zoomDiv<=0)
//					iy+=height+zoom*height/zoomDiv;
				
				//when in navigation mode, move image with cursor
				if (navigating) {
					int mxNew=MouseInfo.getPointerInfo().getLocation().x, myNew=MouseInfo.getPointerInfo().getLocation().y;
					ix+=mxNew-mx;
					iy+=myNew-my;
					mx=mxNew;
					my=myNew;
				}
				
				//switch main image to largest on screen
				if (ix-(width+zoom*width/zoomDiv)>0&&ix+width+zoom*width/zoomDiv>width) 
					ix=ix-(width+zoom*width/zoomDiv);
				else if (ix+width+zoom*width/zoomDiv<width&&ix<0) 
					ix=ix+width+zoom*width/zoomDiv;
				
				if (iy-(height+zoom*height/zoomDiv)>0&&iy+height+zoom*height/zoomDiv>height) 
					iy=iy-(height+zoom*height/zoomDiv);
				else if (iy+height+zoom*height/zoomDiv<height&&iy<0) 
					iy=iy+height+zoom*height/zoomDiv;
				
				//draw background and main map
				g.setColor(new Color(50,50,50));
				g.fillRect(0, 0, screensize[0], screensize[1]);
				g.drawImage(img,ix,iy,width+zoom*width/zoomDiv,height+zoom*height/zoomDiv,this);
				
				//draw side and corner maps if screen is not filled by main map
				if (ix>0) {
					g.drawImage(img,ix-(width+zoom*width/zoomDiv),iy,width+zoom*width/zoomDiv,height+zoom*height/zoomDiv,this);
					if (iy>0) 
						g.drawImage(img,ix-(width+zoom*width/zoomDiv),iy-(height+zoom*height/zoomDiv),width+zoom*width/zoomDiv,height+zoom*height/zoomDiv,this);
					if (iy+height+zoom*height/zoomDiv<height) 
						g.drawImage(img,ix-(width+zoom*width/zoomDiv),iy+height+zoom*height/zoomDiv,width+zoom*width/zoomDiv,height+zoom*height/zoomDiv,this);
				}
				if (ix+width+zoom*width/zoomDiv<width) {
					g.drawImage(img,ix+width+zoom*width/zoomDiv,iy,width+zoom*width/zoomDiv,height+zoom*height/zoomDiv,this);
					if (iy>0) 
						g.drawImage(img,ix+width+zoom*width/zoomDiv,iy-(height+zoom*height/zoomDiv),width+zoom*width/zoomDiv,height+zoom*height/zoomDiv,this);
					if (iy+height+zoom*height/zoomDiv<height) 
						g.drawImage(img,ix+width+zoom*width/zoomDiv,iy+height+zoom*height/zoomDiv,width+zoom*width/zoomDiv,height+zoom*height/zoomDiv,this);
				}
				if (iy>0) 
					g.drawImage(img,ix,iy-(height+zoom*height/zoomDiv),width+zoom*width/zoomDiv,height+zoom*height/zoomDiv,this);
				if (iy+height+zoom*height/zoomDiv<height) 
					g.drawImage(img,ix,iy+height+zoom*height/zoomDiv,width+zoom*width/zoomDiv,height+zoom*height/zoomDiv,this);
			}
		};
		canvas.addMouseListener(new MouseListener() {
			@Override
			public void mousePressed(MouseEvent e) { //activate navigation mode
				navigating=true;
				mx=MouseInfo.getPointerInfo().getLocation().x;
				my=MouseInfo.getPointerInfo().getLocation().y;
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) { //stop navigation mode
				navigating=false;
			}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}
		});
		
		canvas.addMouseWheelListener(new MouseWheelListener(){

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) { //zooms in on mouse cursor location
				if (!e.isControlDown()) { // scrolling
					if (zoom<=zoomMin&&e.getWheelRotation()<0)
						return;
					else if (zoom>=zoomMax&&e.getWheelRotation()>0) 
						return;
					
					int ow=width+zoom*width/zoomDiv, oh=height+zoom*height/zoomDiv; //old image dimensions
					
					zoom+=e.getWheelRotation(); // negative = down, positive = up
					if (zoom<zoomMin)
						zoom=zoomMin;
					else if (zoom>zoomMax) 
						zoom=zoomMax;
					
					int nw=width+zoom*width/zoomDiv, nh=height+zoom*height/zoomDiv; //new image dimensions
					
					//calculate new image coords relative to cursor location and zoom
					ix-=(ix+nw-e.getX())-(ix+ow-e.getX())*(nw/(double)ow); 
					iy-=(iy+nh-e.getY())-(iy+oh-e.getY())*(nh/(double)oh);
				} 
			}
			
		});
		
		canvas.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) { //center mouse
				try {
					Robot robot = new Robot();
					robot.mouseMove(width/2, height/2);
				} catch (AWTException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {}
			
		});
		
		canvas.setFocusable(true);
		
		frame.add(canvas);
		frame.setLocationRelativeTo(null);
		frame.setResizable(true);
		frame.setVisible(true);
		try {
			Thread.sleep(250);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		frame.getContentPane().repaint();
		while (true) {
			try {
				Thread.sleep(15);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			frame.getContentPane().repaint(); //new frame every 15ms
		}
	}
	
	public static void main(String[] args) {
		new MapDisplay();
	}
}
