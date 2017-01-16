package Pacman;
//Made by Andrew Xue
//a3xue@edu.uwaterloo.ca
//PACMAN! THIS GAME IS NOT FINISHED. Use the arrow keys to control PacMan. This game
//    uses a Queue ADT so you can enter multiple commands pre-emptively. The movement
//    of the red square is produced using a pathfinding algorithm which finds the most
//    efficient path to pacman's position and follows that.
//Part of a project to learn Java over the winter break and create retro video games

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class Pacman {
	JFrame window = new JFrame("Pacman");
	static ArrayList<String> movequeueADT = new ArrayList<String>();
	static ArrayList<ArrayList<Integer>> explored = new ArrayList<ArrayList<Integer>>();
	static int[][] gamestate = new int[31][28];
	static int[][] dirlist = {{-1,0},{0,-1},{1,0},{0,1}}; //0=up 1=left 2=down 3=right
	static String[] dirtostring = {"up","left","down","right"};
	static int[][][] biglist=
		{{{9,9,10},{16,9,10},{0,24,25},{11,12,16},{14,12,16},{25,24,25}},
		{{8,13,14},{20,13,14},{23,4,5,22,23},{24,7,8,19,20},{26,13,14}},
		{{2,2,3,4,6,7,21,22},{22,2,3,4,6,7,21,22},{1,9,19},{23,9,19},{10,6,7,18,19,24,25},{14,6,7,18,19,24,25}},
		{{1,13,14},{6,7,8,19,20},{10,7,8,19,20},{26,0,27}},
		{{0,13,15},{7,2,3,4,21,22,27,28},{2,27,28},{16,2,3,4,21,22,27,28},{21,27,28},{23,13,15}},
		{{0,0,27},{5,0,27},{9,5,22},{15,5,22},{19,0,27},{15,7,8,19,20},{12,10,17}}};
	// biglist is the base of the gamestate and contains data for all the blocks which you cannot move on
	// the biglist is a list of {listofhorizontalblocksoflength3, listofverticalblocksoflength3,
	//           listofhorizontalblocksoflength4, listofverticalblocksoflength4,
	//			 listofhorizontalblocksoflength5, listofverticalblocksoflength5}
	// where a listofhorizontalblocksX is a list of lists each of which contain {x, n1,n2...nk} elements
	// which in turn represent a horizontal row of blocks of X length starting at point (x,n1) for all nk
	// the same for verticalblocks except (n1,x)
	
	String pacdir="left";
	int pacx=(120+(20*13));
	int pacy=(30+(20*23));
	
	int redx=380;
	int redy=250;
	
	int orangex=400;
	int orangey=250;
	
	int reddir;
	int nextdir;
	int orangedir;
	int targetx;
	int targety;

	// 31 deep, 28 wide
	public static void main(String[] args){
		// Places the blocks which cannot be moved on onto the gamestate
		for (int x=1; x<31; x++){
			for (int y=1; y<28;y++){
				if (x<=8||x>=20)gamestate[x][y]=-2;
				else {gamestate[x][6]=2; gamestate[x][21]=-2;}
			}
		}
		for (int x=0; x<28;x++){
			gamestate[0][x]=-1;
			gamestate[30][x]=-1;
		}
		// Code for implementing the biglist onto the gamestate
		for (int x=0; x<=4; x+=2){
			for (int doub=0; doub<=1; doub++){
				if (doub==0){
				for (int j=0; j<(3+(x/2)); j++){
						for (int k=0;k<biglist[x].length;k++){
							int initial = biglist[x][k][0];
							for (int y=1; y<biglist[x][k].length;y++){
								gamestate[biglist[x][k][y]][initial+j]=-1;
							}
						}
					}
				}
				else if (doub==1){
					for (int j=0; j<(3+(x/2)); j++){
					for (int k=0;k<biglist[x+1].length;k++){
						int initial = biglist[x+1][k][0];
						for (int y=1; y<biglist[x+1][k].length;y++){
							gamestate[initial+j][biglist[x+1][k][y]]=-1;
						}
					}
				}
				}
			}
		}		
		new Pacman().go();
	}
	
	// Adds a KeyListener that responds to any arrow key being pressed by adding it to the queueADT
	private class keyactions implements KeyListener {
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode()==KeyEvent.VK_UP&&pacdir!="up")movequeueADT.add("up");
			if (e.getKeyCode()==KeyEvent.VK_DOWN&&pacdir!="down")movequeueADT.add("down");
			if (e.getKeyCode()==KeyEvent.VK_LEFT&&pacdir!="left")movequeueADT.add("left");
			if (e.getKeyCode()==KeyEvent.VK_RIGHT&&pacdir!="right")movequeueADT.add("right");}
		public void keyReleased(KeyEvent arg0) {}
		public void keyTyped(KeyEvent e) {}
		}
	
	// creates a window and adds all the elements of the game to it. Also implements a KeyListener
	//    and initiates the movement of all the elements
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
			// teleports pacman if it is on one of the edges
			if (pacdir=="left"&&pacx<120) pacx=660;
			if (pacdir=="right"&&pacx>655) pacx=120;
			// checks if pacman is able to change directions to the first of the queueADT
			if (movequeueADT.size()>=1){
				if (!blocked(movequeueADT.get(0),pacy,pacx)){
					pacdir=movequeueADT.get(0);
					movequeueADT.remove(0);
				}
				// removes elements from the queue ADT in the case that pacman is stuck in a corner
				if (blocked(pacdir,pacy,pacx)&&blocked(movequeueADT.get(0),pacy,pacx)) movequeueADT.remove(0);
			}
			// move pacman based on the pacdir, a variable which dictates the direction pacman is travelling
			if (pacdir=="left"&&!blocked("left",pacy,pacx))pacx-=1;
			if (pacdir=="right"&&!blocked("right",pacy,pacx))pacx+=1;
			if (pacdir=="up"&&!blocked("up",pacy,pacx))pacy-=1;
			if (pacdir=="down"&&!blocked("down",pacy,pacx))pacy+=1;
			// pacman eats the dots if it is occupying the same grid space as the dot
			if (gamestate[(pacy-20)/20][(pacx-105)/20]==-2)gamestate[(pacy-30)/20][(pacx-120)/20]=0;
			
			try {Thread.sleep(10);} catch(Exception exp){System.out.println("Runtime Error");}
			// finds the x and y grid coordinates of pacman's position
			targetx=(pacx-105)/20;
			targety=(pacy-20)/20;
			
			//Finds the new direction that the red ghost should travel in using the pathfinding algorithm
			// The red ghost will simply take the more efficient path to pacman
			int newreddir=(explorer((redx-105)/20, (redy-20)/20,reddir));
			if (!blocked(dirtostring[newreddir],redy,redx))reddir=newreddir;
			redx+=(dirlist[reddir][1]);
			redy+=(dirlist[reddir][0]);
			
			//Finds the new direction that the red ghost should travel in using the pathfinding algorithm
			//The orangeghost will attempt to flank pacman by blocking off the closest intersection behind it
			//Unfortunately very buggy.

/*			int [] flanklst=flanker((orangex-105)/20, (orangey-20)/20);
			targetx=flanklst[0];
			targety=flanklst[1];
			int neworangedir=(explorer((orangex-105)/20, (orangey-20)/20,orangedir));
			if (!blocked(dirtostring[neworangedir],orangey,orangex))orangedir=neworangedir;
			orangex+=(dirlist[orangedir][1]);
			orangey+=(dirlist[orangedir][0]);*/

			window.repaint();
		}
	}
	// Outputs the coordinates of the closest intersection behind pacman
	private int[] flanker(int xcoord, int ycoord){
		int [] flank={targetx,targety};
		if (pacdir=="left"){
			if (targetx==26){
				flank[0]=26;
				flank[1]=targety;
				return flank;}
			else {
				for (int x=targetx; x<=26;x++){
					if (gamestate[targety-1][x]!=-1||gamestate[targety+1][x]!=-1){
						flank[0]=x;
						flank[1]=targety;
						return flank;
					}
				}
			}
		}
		if (pacdir=="right"){
			if (targetx==1){
				flank[0]=1;
						
				flank[1]=targety;
				return flank;}
			else {
				for (int x=targetx; x>=1;x--){
					if (gamestate[targety-1][x]!=-1||gamestate[targety+1][x]!=-1){
						flank[0]=x;
						flank[1]=targety;
						return flank;
					}
				}
			}
		}
		if (pacdir=="up"){
				for (int x=targety; x>=1;x--){
					if (gamestate[x][targetx-1]!=-1||gamestate[x][targetx+1]!=-1){
						flank[0]=x;
						flank[1]=targety;
						return flank;
					}
				}
		}
		if (pacdir=="down"){
			for (int x=targety; x<=26;x++){
				if (gamestate[x][targetx-1]!=-1||gamestate[x][targetx+1]!=-1){
					flank[0]=x;
					flank[1]=targety;
					return flank;
				}
			}
	}
		return flank;
		
	}
	// Checks if the piece is able to keep moving or if there is a block in front of it
	private boolean blocked(String dir,int ycoord,int xcoord){
		if (dir=="left"&&(gamestate[((ycoord-30)/20)][((xcoord-121)/20)]==-1
				||gamestate[((ycoord-11)/20)][((xcoord-121)/20)]==-1)) return true;
		
		if (dir=="right"&&(gamestate[((ycoord-30)/20)][((xcoord-120+20)/20)]==-1||
				gamestate[((ycoord-11)/20)][((xcoord-120+20)/20)]==-1)) return true;
		
		if (dir=="up"&&(gamestate[((ycoord-31)/20)][((xcoord-120)/20)]==-1||
				gamestate[((ycoord-31)/20)][((xcoord-101)/20)]==-1)) return true;
		
		if (dir=="down"&&(gamestate[((ycoord-10)/20)][((xcoord-120)/20)]==-1||
				gamestate[((ycoord-10)/20)][((xcoord-101)/20)]==-1)) return true;
		return false;
	}
	// Draws the board and all the elements on it
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
					if (gamestate[x][y]==-1)
					grap.fillRect(120+(20*y), (30+(20*x)), 18, 18);
				}
			}
			grap.setColor(Color.RED);
			grap.fillRect(redx, redy, 20, 20);
			for (int x=0; x<31;x++){
				for (int y=0;y<28;y++){
					if (gamestate[x][y]==-2)
					grap.fillRect(120+(20*y)+8, (30+(20*x))+8, 4, 4);
				}
			}
			grap.setColor(Color.ORANGE);
			grap.fillRect(orangex, orangey, 20, 20);

			grap.setColor(Color.YELLOW);
			grap.fillOval(pacx, pacy, 20, 20);
			//{23,13}
		}
	}
	
	// Pathfinding algorithm. Finds the most efficient path from one grid coordinate
	// to another and outputs the direction of the first step of this path
	int explorer(int xcoord, int ycoord, int dirtype){
		int[][] newgamestate = new int[31][28];
		for (int x=0; x<gamestate.length;x++){
			for (int y=0;y<gamestate[0].length;y++){
				if (gamestate[x][y]==-1)
				newgamestate[x][y]=-1;
				else newgamestate[x][y]=0;
			}
		}
		for (int x=0; x<4; x++){
			if (x!=(dirtype+2)%4&&xcoord+dirlist[x][1]>=0&&xcoord+dirlist[x][1]<=27&&
					newgamestate[ycoord+dirlist[x][0]][xcoord+dirlist[x][1]]!=-1){
				newgamestate[ycoord+dirlist[x][0]][xcoord+dirlist[x][1]]=0;
				explore(ycoord+dirlist[x][0],xcoord+dirlist[x][1],1,x,newgamestate);
			}
		}
		return nextdir;
	}
	void explore(int ycoord, int xcoord, int acc, int dir,int[][] gamestate){
		for (int x=0; x<4; x++){
			if (xcoord+dirlist[x][1]>=0&&xcoord+dirlist[x][1]<=27){
			int next = (gamestate[ycoord+dirlist[x][0]][xcoord+dirlist[x][1]]);
				if (next==0||next>acc){
					if (ycoord==targety && xcoord==targetx)	nextdir=dir; 
					gamestate[ycoord+dirlist[x][0]][xcoord+dirlist[x][1]]=acc;
					explore(ycoord+dirlist[x][0],xcoord+dirlist[x][1],acc+1,dir,gamestate);
				}
		}
		}
	}
}
