package Pacman;
//Made by Andrew Xue
//a3xue@edu.uwaterloo.ca
//PACMAN! THIS GAME IS NOT FINISHED. Use the arrow keys to control PacMan. This game
//    uses a Queue ADT so you can enter multiple commands pre-emptively. 
//Part of a project to learn Java over the winter break and create retro video games

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class Pacman {
	JFrame window = new JFrame("Pacman");
	static ArrayList<String> movequeueADT = new ArrayList<String>();
	static int[][] gamestate = new int[31][28];
	static int[][][] biglist=
		{{{9,9,10},{16,9,10},{0,24,25},{11,12,16},{14,12,16},{25,24,25}},
		{{8,13,14},{20,13,14},{23,4,5,22,23},{24,7,8,19,20},{26,13,14}},
		{{2,2,3,4,6,7},{22,2,3,4,6,7},{1,9,13,15,19},{23,9,13,15,19},{10,6,7,18,19,24,25},{14,6,7,18,19,24,25},{2,21,22},{22,21,22}},
		{{1,13,14},{6,7,8,19,20},{10,7,8,19,20}},
		{{7,2,3,4,21,22,27,28},{2,27,28},{16,2,3,4,21,22,27,28},{21,27,28}},
		{{15,7,8,19,20},{12,10,17}}};
	
	String pacdir="left";
	int pacx=(120+(20*13));
	int pacy=(30+(20*23));

	// 31 deep, 28 wide
	public static void main(String[] args){
		for (int x=1; x<31; x++){
			for (int y=1; y<28;y++){
				if (x<=8||x>=20)gamestate[x][y]=2;
				else {gamestate[x][6]=2; gamestate[x][21]=2;}
			}
		}
		
		for (int x=0; x<28;x++){
			gamestate[0][x]=1;
			gamestate[30][x]=1;
		}
		for (int x=0; x<31; x++){
			if ((x>=10&&x<=12)||(x<=18&&x>=16)){
				gamestate[x][5]=1;
				gamestate[x][22]=1;
			}
			else if (x<10||x>18){
			gamestate[x][0]=1;
			gamestate[x][27]=1;}
		}
		
		gamestate[9][5]=1;
		gamestate[13][5]=1;
		gamestate[15][5]=1;
		gamestate[19][5]=1;
		
		gamestate[9][22]=1;
		gamestate[13][22]=1;
		gamestate[15][22]=1;
		gamestate[19][22]=1;
		
		gamestate[13][0]=1;
		gamestate[15][0]=1;
		
		gamestate[13][27]=1;
		gamestate[15][27]=1;
		
		for (int x=0; x<=4; x+=2){
			for (int doub=0; doub<=1; doub++){
				if (doub==0){
				for (int j=0; j<(3+(x/2)); j++){
						for (int k=0;k<biglist[x].length;k++){
							int initial = biglist[x][k][0];
							for (int y=1; y<biglist[x][k].length;y++){
								gamestate[biglist[x][k][y]][initial+j]=1;
							}
						}
					}
				}
				else if (doub==1){
					for (int j=0; j<(3+(x/2)); j++){
					for (int k=0;k<biglist[x+1].length;k++){
						int initial = biglist[x+1][k][0];
						for (int y=1; y<biglist[x+1][k].length;y++){
							gamestate[initial+j][biglist[x+1][k][y]]=1;
						}
					}
				}

				}
			}
		}		
		new Pacman().go();
	}
	
	private class keyactions implements KeyListener {
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode()==KeyEvent.VK_UP&&pacdir!="up")movequeueADT.add("up");
			if (e.getKeyCode()==KeyEvent.VK_DOWN&&pacdir!="down")movequeueADT.add("down");
			if (e.getKeyCode()==KeyEvent.VK_LEFT&&pacdir!="left")movequeueADT.add("left");
			if (e.getKeyCode()==KeyEvent.VK_RIGHT&&pacdir!="right")movequeueADT.add("right");}
		public void keyReleased(KeyEvent arg0) {}
		public void keyTyped(KeyEvent e) {}
		}
	void go() {
		window.setSize(815, 720);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		window.setResizable(false);
		window.add(new pacgrid());
		window.repaint();
		window.addKeyListener(new keyactions());
		pacmove();
	}
	
	private void pacmove(){
		
		while (true){
			if (pacdir=="left"&&pacx<120) pacx=660;
			if (pacdir=="right"&&pacx>655) pacx=120;
			if (movequeueADT.size()>=1){
				if (!blocked(movequeueADT.get(0))){
					pacdir=movequeueADT.get(0);
					movequeueADT.remove(0);
				}
				if (blocked(pacdir)&&blocked(movequeueADT.get(0))) movequeueADT.remove(0);
			}
			
			if (pacdir=="left"&&!blocked("left"))pacx-=1;
			if (pacdir=="right"&&!blocked("right"))pacx+=1;
			if (pacdir=="up"&&!blocked("up"))pacy-=1;
			if (pacdir=="down"&&!blocked("down"))pacy+=1;
			
			if (gamestate[(pacy-20)/20][(pacx-110)/20]==2)gamestate[(pacy-30)/20][(pacx-120)/20]=0;
			
			try {Thread.sleep(10);} catch(Exception exp){System.out.println("Runtime Error");}
			window.repaint();
		}
	}
	// 31 deep, 28 wide
	private boolean blocked(String dir){
		
		
		if (dir=="left"&&(gamestate[((pacy-30)/20)][((pacx-121)/20)]==1
				||gamestate[((pacy-11)/20)][((pacx-121)/20)]==1)) return true;
		
		if (dir=="right"&&(gamestate[((pacy-30)/20)][((pacx-120+20)/20)]==1||
				gamestate[((pacy-11)/20)][((pacx-120+20)/20)]==1)) return true;
		
		if (dir=="up"&&(gamestate[((pacy-31)/20)][((pacx-120)/20)]==1||
				gamestate[((pacy-31)/20)][((pacx-101)/20)]==1)) return true;
		
		if (dir=="down"&&(gamestate[((pacy-10)/20)][((pacx-120)/20)]==1||
				gamestate[((pacy-10)/20)][((pacx-101)/20)]==1)) return true;

		// 23 dead 5 right
		return false;
	}
	
	private class pacgrid extends JComponent {
		public void paintComponent(Graphics g){
			Graphics2D grap = (Graphics2D) g;
			// grid size 28x36
			grap.setColor(Color.RED);
			grap.fillRect(0, 0, 815,720);
			grap.setColor(Color.BLACK);
			grap.fillRect(5, 5, 800, 675);
			grap.setColor(Color.BLUE);
			for (int x=0; x<31;x++){
				for (int y=0;y<28;y++){
					if (gamestate[x][y]==1)
					grap.fillRect(120+(20*y), (30+(20*x)), 20, 20);
				}
			}
			grap.setColor(Color.RED);
			for (int x=0; x<31;x++){
				for (int y=0;y<28;y++){
					if (gamestate[x][y]==2)
					grap.fillRect(120+(20*y)+8, (30+(20*x))+8, 4, 4);
				}
			}
			grap.setColor(Color.YELLOW);
			grap.fillOval(pacx, pacy, 20, 20);
			//{23,13}
		}
		}
}
