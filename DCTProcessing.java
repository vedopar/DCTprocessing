import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;

public class DCTProcessing {
	public static void main(String[] args){
		DCTProcessing a=new DCTProcessing(args);
	}
	DCTProcessing(String[] args){
		source_m=new int[channels][height][width];
		dct_m=new int[channels][height][width];
		output_m=new int[channels][height][width];
		if(readArgs(args)==false)
			return;
		//System.out.println(source_name+" "+quan+" "+delivery_mode+" "+latency);
		if(readSource()==false){
			System.out.println("Image file not valid!");
			return;
		}
		initCos();
		dct();
		//matrixToImage(dct_m,"dct.png");
		//inDct();
		showOutput();
		//matrixToImage(output_m,"output.png");
		//for(int j=0;j<height;j++){
		//	System.out.println('\n');
		//	for(int i=0;i<width;i++)
		//		System.out.print(output_m[0][j][i]+" ");
		//}
	}
	private boolean readArgs(String[] args){
		if (args.length >= 4) {
		    try {
		    	source_name=args[0];
		    	quan =1<< Integer.parseInt(args[1]);
		    	if(quan<1 || quan>128)
		    	{
		    		System.out.println("Quantization level not valid!");
		    		return false;
		    	}
		    	delivery_mode=Integer.parseInt(args[2]);
		    	if(delivery_mode<1 || delivery_mode>3)
		    	{
		    		System.out.println("Delivery mode not valid!");
		    		return false;
		    	}
		    	latency=Integer.parseInt(args[3]);
		    	if(latency<0)
		    	{
		    		System.out.print("latency not valid!");
		    		return false;
		    	}
		    	return true;
		    } catch (NumberFormatException e) {
		        System.err.println("Argument" + " must be an integer");
		        return false;
		    }
		}
		System.out.println("lack args!");
		return false;
	}
	private boolean readSource(){
		BufferedImage source_im = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    try {
		    File file = new File(source_name);
		    InputStream is = new FileInputStream(file);
		    long len = file.length();
		    byte[] bytes = new byte[(int)len];
		    int offset = 0;
	        int numRead = 0;
	        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	            offset += numRead;
	        }	
	    	int ind = 0;
			for(int y = 0; y < height; y++){
		
				for(int x = 0; x < width; x++){
			 
					byte r,g,b;
					r = bytes[ind];
					source_m[0][y][x]=r&0xff;
					//source_m[0][y][x]=(byte) (r-128);
					g = bytes[ind+height*width];
					source_m[1][y][x]= g&0xff;
					//source_m[1][y][x]= (byte) (g-128);
					b = bytes[ind+height*width*2]; 
					source_m[2][y][x]=b&0xff; 
					//source_m[2][y][x]=(byte) (b-128);
					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					source_im.setRGB(x,y,pix);
					ind++;
				}
			}
			File outputfile = new File("source.png");
		    ImageIO.write(source_im, "png", outputfile);
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	      return false;
	    } catch (IOException e) {
	      e.printStackTrace();
	      return false;
	    }
	    return true;
	}
	private void initCos(){
		cos_value=new double[scale][scale][scale][scale];
		double pi=Math.PI;
		for(int i1=0;i1<scale;i1++){
		    int i1_2=2*i1+1;
		    for(int j1=0;j1<scale;j1++){
		        for(int i2=0;i2<scale;i2++){
		            int i2_2=2*i2+1;
		            for(int j2=0;j2<scale;j2++){
		                cos_value[j1][j2][i1][i2]=(Math.cos(pi*i1_2*j1*0.0625)*Math.cos(pi*i2_2*j2*0.0625));
		            }
		        }
		    }
		}
	}
	private void dct(){
	    for(int which=0;which<channels;which++){
	    	for(int y=0;y<s_height;y++){
	    		for(int x=0;x<s_width;x++){
	    			for(int v=0;v<scale;v++){
	    				for(int u=0;u<scale;u++){
	    					double summary=0;
	    					double c=0;
	    					if(u!=0 && v!=0)
	    						c=0.25;
	    					else if((u==0&&v!=0)||(u!=0&&v==0))
	    						c=0.25*0.707;
	    					else
	    						c=0.125;
	    					for(int j=0;j<scale;j++){
	    						for(int i=0;i<scale;i++)
	    							summary=summary+source_m[which][j+y*scale][i+x*scale]*cos_value[v][u][j][i];
	    					}
	    					dct_m[which][v+y*scale][u+x*scale]=(int)(summary*c/quan);
	    					//dct_tmp_m[which][v+y*scale][u+x*scale]=0;
	    				}
	    			}
	    		}
	    	}
	    }
	}
	
	//x y is the coordinates in the matrix, im is the image, 
	//limit is used for delivery mode 2 as the number of coefficients
	//limit2 is used for delivery mode 3 as the number of bits
	private void inDct(int y,int x,BufferedImage im,int limit,int limit2){
		int get_bits=0;
		if(delivery_mode==3){
			int count=0;
			for(int i=4*scale;i>=1;i--){
				get_bits+=(1<<i);
				count++;
		if(count==limit2)
			break;
			}
		}
		int[] significance;//this is for delivery mode 3
	    for(int which=0;which<channels;which++){
	    	int sh=0;//this is for specifically changing R G or B value
	    	int sh2=0;
	    	if(which==0){
	    		sh=0xff00ffff;
	    		sh2=0x00ff0000;
	    	}
	    	else if(which==1){
	    		sh=0xffff00ff;
	    		sh2=0x0000ff00;
	    	}
	    	else if(which ==2){
	    		sh=0xffffff00;
	    		sh2=0x000000ff;
	    	}
	    			for(int j=0;j<scale;j++){		
	    				for(int i=0;i<scale;i++){
	    					double summary=0;//for the adding sum of the block
	    					double c=0;//this is for the 1/4*C(u)*C(v)
	    					int v=0;
	    					int u=0;
	    					int count=0;//this is for the limit count
	    					boolean direction=true;//used to guide the direction of the zig-zag routine
	    					if(delivery_mode==3){}
	    						for(;u<scale || v<scale;){//here iterate the matrix in a zig-zag routine
	    	    					if(u!=0 && v!=0)
	    	    						c=0.25;
	    	    					else if((u==0&&v!=0)||(u!=0&&v==0))
	    	    						c=0.25*0.707;
	    	    					else
	    	    						c=0.125;
	    	    					
	    	    					if(delivery_mode==3)
	    	    						summary=summary+(dct_m[which][v+y][x+u]&get_bits)*cos_value[v][u][j][i]*c;
	    	    					else
	    	    						summary=summary+dct_m[which][v+y][x+u]*cos_value[v][u][j][i]*c;
	    							if(count++==limit)
	    								break;
	    							if(direction){
	    								if(u==scale-1){
	    									v++;
	    									direction=false;
	    									continue;
	    								}
	    								if(v==0){
	    									u++;
	    									direction=false;
	    									continue;
	    								}
	    								v--;
	    								u++;
	    								continue;
	    							}
	    							else{
	    								if(v==scale-1){
	    									u++;
	    									direction=true;
	    									continue;
	    								}
	    								if(u==0){
	    									v++;
	    									direction=true;
	    									continue;
	    								}
	    								u--;
	    								v++;
	    								continue;
	    							}
	    						}
	    					output_m[which][j+y][i+x]=(int)(summary*quan);
	    					//System.out.print(which_row2[i+i_base]+" ");
	    					int rgb=im.getRGB(x+i, y+j)&sh;
	    					im.setRGB(x+i, y+j, rgb|((output_m[which][j+y][i+x]<<(8*(2-which))&sh2)));
	    				}
	    			}
	    		}    		
	}
	
	
	private BufferedImage matrixToImage(int[][][] matrix){
		BufferedImage im=new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for(int y = 0; y < height; y++){	
			for(int x = 0; x < width; x++){
				//byte r = (byte) (matrix[0][y][x]+128);
				//byte g = (byte) (matrix[1][y][x]+128);
				//byte b = (byte) (matrix[2][y][x]+128); 
				byte r = (byte) (matrix[0][y][x]);
				byte g = (byte) matrix[1][y][x];
				byte b = (byte) matrix[2][y][x]; 
				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				im.setRGB(x,y,pix);
			}
		}
		return im;
		/*File outputfile = new File(image_name);
	    try {
			ImageIO.write(im, "png", outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
	private void showOutput(){
		BufferedImage output_im = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    JFrame frame = new JFrame();
	    JLabel label = new JLabel(new ImageIcon(matrixToImage(source_m)));
	    frame.getContentPane().add(label, BorderLayout.WEST);
	    //JLabel label2 = new JLabel(new ImageIcon());
	    JLabel label2 = new JLabel(new ImageIcon(output_im));
	    frame.getContentPane().add(label2, BorderLayout.EAST);
	    frame.pack();
	    frame.setVisible(true);
	    int limit=0;
	    
	    if(delivery_mode==1){
	    	if(latency==0){
		    for(int j=0;j<s_height;j++){
		    	for(int i=0;i<s_width;i++){
		    		inDct(j*scale,i*scale,output_im,block_length-1,-1);
		    	}
		    }
    	    label2 = new JLabel(new ImageIcon(output_im));
    	    frame.getContentPane().add(label2, BorderLayout.EAST);
    	    label2.updateUI();
	    	}
	    	else{
	    	    for(int j=0;j<s_height;j++){
	    	    	for(int i=0;i<s_width;i++){
	    	    		try {
	    					inDct(j*scale,i*scale,output_im,block_length-1,-1);
	    				    label2 = new JLabel(new ImageIcon(output_im));
	    				    frame.getContentPane().add(label2, BorderLayout.EAST);
	    				    label2.updateUI();
	    					Thread.sleep(latency);
	    				} catch (InterruptedException e) {
	    					// TODO Auto-generated catch block
	    					e.printStackTrace();
	    				}
	    	    	}
	    	    }
	    	}
	    }
	    else if(delivery_mode==2){
	    		while(limit<(block_length)){

	    			for(int j=0;j<s_height;j++){
	    				for(int i=0;i<s_width;i++){
	    						inDct(j*scale,i*scale,output_im,limit,-1);
	    				}
	    			}
	    			limit++;
					label2 = new JLabel(new ImageIcon(output_im));
					frame.getContentPane().add(label2, BorderLayout.EAST);
					label2.updateUI();
					try {
						Thread.sleep(latency);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	    		}
	    		//System.out.println("end here,limit is"+limit);
	    	}
	    else{
    		while(limit++<=(4*scale)){

    			for(int j=0;j<s_height;j++){
    				for(int i=0;i<s_width;i++){
    						inDct(j*scale,i*scale,output_im,block_length-1,limit);
    				}
    			}
				label2 = new JLabel(new ImageIcon(output_im));
				frame.getContentPane().add(label2, BorderLayout.EAST);
				label2.updateUI();
				try {
					Thread.sleep(latency);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
	    }
	}
	
	private int delivery_mode=-1;
	private String source_name="image1.rgb";
	private int[][][] source_m;
	private int[][][] dct_m;
	private double[][][][] cos_value;
	private int[][][] output_m;
	private long latency=-1;
	final private int width=352;
	final private int height=288;
	final private int scale=8;
	final private int block_length=scale*scale;
	final private int half_matrix=36;
	final private int s_width=width/scale;
	final private int s_height=height/scale;
	private int quan=0;
	final private int channels=3;
}
