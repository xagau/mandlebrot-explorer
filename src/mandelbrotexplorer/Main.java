/**
    <one line to give the program's name and a brief idea of what it does.>
    Copyright (C) 2010  Sean Beecroft, Cay Horstmann

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 * @version 1.10 2010-01-01
 * @author Cay Horstmann, Sean Beecroft
 */

package mandelbrotexplorer;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Main {
	public static void main(String[] args) {
		JFrame frame = new MandelbrotFrame();
		frame.setVisible(true);

	}
}

class MandelbrotFrame extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public MandelbrotFrame() {
		setTitle("Mandelbrot Explorer version 1.2");
		setSize(400, 400);
		//Authors: Sean Beecroft, Cay Horstmann
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		final JFrame j = this;
		JMenuBar menubar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenuItem about = new JMenuItem("About");
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				System.exit(-1);
			}
		});

		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				JOptionPane.showMessageDialog(j, "This application was written by Sean Beecroft in 2010,\n based on original work by Dr. Cay Horstmann in 1999", "About Mandelbrot Explorer", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		file.add(about);
		file.add(exit);
		menubar.add(file);
		this.setJMenuBar(menubar);

		Container contentPane = getContentPane();
		MandelbrotPanel mp = new MandelbrotPanel();
		contentPane.add(mp, "Center");

	}
}

class Position {

	public double XMIN = 0;
	public double XMAX = 0;
	public double YMIN = 0;
	public double YMAX = 0;

	public Position(double XMIN, double XMAX, double YMIN, double YMAX) {
		this.XMIN = XMIN;
		this.XMAX = XMAX;
		this.YMIN = YMIN;
		this.YMAX = YMAX;
	}
}

class MandelbrotPanel extends JPanel implements ComponentListener,
		MouseMotionListener, MouseListener, KeyListener {
	Stack path = new Stack();

	/**
	 * 
	 */
	public MandelbrotPanel() {
		this.addComponentListener(this);
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.addKeyListener(this);

		this.setFocusable(true);

		Position p = new Position(XMIN, XMAX, YMIN, YMAX); // put the starting

		// position.
		path.push(p);

	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image == null || resized) {
			image = new BufferedImage(getWidth(), getHeight(),
					BufferedImage.TYPE_INT_ARGB);
			generate();
			resized = false;
		}
		g.drawImage(image, 0, 0, null);
		g.drawRect(rx - rw / 2, ry - rh / 2, rw, rh);

		g.drawLine(0, ry, getWidth(), ry);
		g.drawLine(rx, 0, rx, getHeight());

		//t++;

		drawLocation(g);

	}

	public void drawLocation(Graphics g) {
		double lXMIN = translate(0, getWidth(), rx - rw / 2, XMIN, XMAX);
		double lYMIN = translate(0, getHeight(), ry - rh / 2, YMIN, YMAX);

		double lXMAX = translate(0, getWidth(), rx + (rw / 2), XMIN, XMAX);
		double lYMAX = translate(0, getHeight(), ry + (rh / 2), YMIN, YMAX);

		int lw = getWidth() - 400;
		int lh = 20;
		int infx = 0;
		int infy = lh;
		String inf = "" + (rx - rw / 2) + "," + (ry - rh / 2) + " [" + lXMIN
				+ "," + lYMIN + "|" + lXMAX + "," + lYMAX + "]";
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), lh);
		g.setColor(Color.black);
		g.drawString(inf, infx, infy - ((lh / 2) - (lh / 4)));

	}

	public void generate() {
		generate(image);
	}

	private void generate(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		WritableRaster raster = image.getRaster();
		ColorModel model = image.getColorModel();

		Color fractalColor = Color.red;

		//MAX_ITERATIONS++;

		// Z = Z + C

		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++) {
				double a = XMIN + (i) * (XMAX - XMIN) / width;
				double b = YMIN + (j) * (YMAX - YMIN) / height;
				fractalColor = escapesToInfinity(a, b);
				if (fractalColor != null) {
					int argb = fractalColor.getRGB();
					Object colorData = model.getDataElements(argb, null);
					raster.setDataElements(i, j, colorData);
				}
			}
	}

	private Color escapesToInfinity(double alpha, double beta) {
		double x = 0.0;
		double y = 0.0;
		double r, g, b;
		double iterations = 0;

		// F = Math.random() / (MAX_ITERATIONS * 1000);
		do {
			double xnew = ((x * x) - y * y) + alpha;
			double ynew = ((2 * x) * y) + beta;
			x = xnew;
			y = ynew;
			iterations+=0.3;
			if (iterations >= MAX_ITERATIONS) {
				Color c = Color.black;
				return c;
			}
		} while (x <= 2 && y <= 2);
		Color col = getColor(iterations+16);
		return col;
	}

	public double normalize(double iterations)
	{
		return iterations ;
	}

	public Color getColor(double iterations)
	{
		iterations = normalize(iterations);
		return Color.getHSBColor((float) iterations / (float) 256, 0.85f, 1);
	}

	public static double translate(double t1, double b1, double p1, double t2,
			double b2) {
		double answer = b2 + ((p1 - b1) / (t1 - b1) * (t2 - b2));
		return answer;
	}

	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void componentResized(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("Called");
		resized = true;
		rw = getWidth() / 4;
		rh = getHeight() / 4;
		repaint();
	}

	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseMoved(MouseEvent me) {
		// TODO Auto-generated method stub
		rx = me.getX();
		ry = me.getY();

		repaint();

	}

	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		XMIN = translate(0, getWidth(), rx - rw / 2, XMIN, XMAX);
		YMIN = translate(0, getHeight(), ry - rh / 2, YMIN, YMAX);

		XMAX = translate(0, getWidth(), rx + (rw / 2), XMIN, XMAX);
		YMAX = translate(0, getHeight(), ry + (rh / 2), YMIN, YMAX);

		Position p = new Position(XMIN, XMAX, YMIN, YMAX);
		cp = p;
		path.push(p);
		generate();
		repaint();
	}

	Position cp = null;

	private static final long serialVersionUID = 1L;
	BufferedImage image = null;
	boolean resized = false;

	int rx, ry, rw, rh;

	private static double XMIN = -2;

	private static double XMAX = 2;

	private static double YMIN = -2;

	private static double YMAX = 2;

	private static int MAX_ITERATIONS = 228;

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent ke) {
		// TODO Auto-generated method stub
		if (ke.getKeyCode() == KeyEvent.VK_UP) {
			System.out.println("KeyUp");
			XMIN = translate(0, getWidth(), rx - rw / 2, XMIN, XMAX);
			YMIN = translate(0, getHeight(), ry - rh / 2, YMIN, YMAX);

			XMAX = translate(0, getWidth(), rx + (rw / 2), XMIN, XMAX);
			YMAX = translate(0, getHeight(), ry + (rh / 2), YMIN, YMAX);

			Position p = new Position(XMIN, XMAX, YMIN, YMAX);
			cp = p;
			path.push(p);

			generate();
			repaint();
			ke.consume();
		}
		if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
			System.out.println("KeyDown");

			// path.pop();
			Position p = (Position) path.pop();
			System.out.println(p);

			XMIN = p.XMIN;
			XMAX = p.XMAX;
			YMIN = p.YMIN;
			YMAX = p.YMAX;

			generate();
			repaint();
			ke.consume();
		}
		if (ke.getKeyCode() == KeyEvent.VK_F) {

			generate();
			repaint();
			ke.consume();
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}
}