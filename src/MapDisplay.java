import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MouseInfo;
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
	
	private int ix=0, iy=0, mx=0, my=0, sx=0, sy=0;
	private int zoom=0;
	private final int zoomMin=-10, zoomMax=20;
	
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
				if (img==null) return;
				if (navigating) {
					int mxNew=MouseInfo.getPointerInfo().getLocation().x, myNew=MouseInfo.getPointerInfo().getLocation().y;
					ix+=mxNew-mx;
					iy+=myNew-my;
					mx=mxNew;
					my=myNew;
				}
				g.setColor(new Color(50,50,50));
				g.fillRect(0, 0, screensize[0], screensize[1]);
				g.drawImage(img,ix,iy,width+zoom*width/20,height+zoom*height/20,this);
//				System.out.println(zoom+" "+(width+zoom*width/20)+" "+(height+zoom*height/20));
			}
		};
		canvas.addMouseListener(new MouseListener() {
			@Override
			public void mousePressed(MouseEvent e) {
				navigating=true;
				mx=MouseInfo.getPointerInfo().getLocation().x;
				my=MouseInfo.getPointerInfo().getLocation().y;
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {
				navigating=false;
			}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}
		});
		
		canvas.addMouseWheelListener(new MouseWheelListener(){

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (!e.isControlDown()) { // scrolling
					if (zoom<=zoomMin&&e.getWheelRotation()<0)
						return;
					else if (zoom>=zoomMax&&e.getWheelRotation()>0) 
						return;
					sx=width+zoom*width/20-e.getX();
					sy=height+zoom*height/20-e.getY();
					System.out.println(e.getX()+" "+e.getY());
					System.out.println(sx+" "+sy);
					zoom+=e.getWheelRotation(); // negative = down, positive = up
					if (zoom<zoomMin)
						zoom=zoomMin;
					else if (zoom>zoomMax) 
						zoom=zoomMax;
				} 
			}
			
		});
		
		canvas.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {}

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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		frame.getContentPane().repaint();
		while (true) {
			try {
				Thread.sleep(15);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			frame.getContentPane().repaint();
		}
	}
	
	public static void main(String[] args) {
		new MapDisplay();
	}
}
