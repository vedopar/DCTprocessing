DCTprocessing
=============

Date: Spring Semester 2013 USC<br/><br/>
Project Info:<br/>
  This is the second project of csci576-Multimedia System Design.<br/>
  This project contains one app which performs Image DCT and inverse<br/>
  DCT transformations in different quantization levels and different<br/>
  delivery modes.<br/><br/>
  command:<br/>
    DCTProcessing InputImage quantizationLevel DeliveryMode Latency<br/>
    
    InputImage:
      is the image(352x288 , must be a rgb file) to input to your coder
      -decoder.
    QuantizationLevel:
      a factor that will decrease/increase compression This value will 
      range from 0 to 7.
    DeliveryMode:
      an index ranging from 1, 2, 3. A 1 implies baseline delivery,a 2 
      implies progressive delivery using spectral selection, a 3 implies 
      progressive delivery using successive bit approximation.
    Latency:
      a variable in milliseconds, which will give a suggestive “sleep” 
      time between data blocks during decoding. This parameter will be used to 
      “simulate” decoding as data arrives across low and high band width 
      communication networks.
      
  Delivery Modes:<br/>
    Sequential Mode:
      Each image block is encoded in a single left-to-right, top-to-bottom scan. You may<br/>
      assume that each latency iteration pertains to ONE BLOCK. So the process progresses as <br/><br/>
        Decode data of first block and display …sleep<br/>
        Decode data of second block and display …sleep<br/>
        …<br/><br/>
    Progressive Mode – Spectral Selection:<br/>
      The DC coefficients of every image blocks is decoded first and displayed. Next the first <br/>
      AC coefficients is added for all the blocks and decoded. This goes on till all the<br/> 
      coefficients are added to the decoding process. You may assume that each latency <br/>
      iteration occurs after EVERY SPECIFIC DCT COFFICIENT for all blocks. So the p<br/>
      rocess progresses as<br/><br/>
        Decode all blocks using only DC coefficient (set rest to zero) …sleep<br/>
        Decode all blocks using only DC, AC1 coefficient …. Sleep<br/>
        Decode all blocks using only DC, AC1, AC2 coefficient …. Sleep<br/>
        …<br/><br/>
    Progressive Mode – Successive Bit Approximation:<br/>
      All DC and AC coefficients of all image blocks are decoded first and displayed in a <br/>
      successive-bit manner. So you will decode all blocks using the all the DC and AC <br/>
      coefficients, but only using the first significant bit of all coefficients Next, you will <br/>
      decode all DC and AC coefficients using the first two significant bits of all coefficients <br/>
      and so on. You may assume that each latency iteration occurs at EACH SIGNIFICANT <br/>
      BIT usage. So the process progresses as<br/><br/>
        Decode all blocks using 1st significant bit of all coefficients …Sleep<br/>
        Decode all blocks using 1st , 2nd significant bit of all coefficients …. Sleep<br/>
        Decode all blocks using 1st , 2nd , 3rd significant bit of all coefficients …. Sleep<br/>
        …<br/><br/>
  
Dependent Libs:<br/>
  Java SE 1.7.0_21
