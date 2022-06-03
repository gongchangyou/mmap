package com.mouse.mmap;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
@SpringBootTest
class MmapApplicationTests {

    String str = "mouse 中文";

    int size = str.getBytes().length;

    MappedByteBuffer buffer;

    @Test
    public void threadTest() {
        val t1 = new Thread(() -> {
            try {
                writeWithMap();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t1.start();

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        val t2 = new Thread(() -> {
                log.info("is Loaded in physical memory: {}",buffer.isLoaded());  //只是一个提醒而不是guarantee
                log.info("capacity {}",buffer.capacity());
                byte[] bytes = new byte[size];

                //read the buffer
                for (int i = 0; i < bytes.length; i++)
                {
                    bytes[i] = buffer.get(i);
//                log.info("get {}", buffer.get());
                }

                val str = new String(bytes);
                log.info("str= {} currentThread={}", str, Thread.currentThread());
        });
        t2.start();

    }

    @Test
    public void writeWithMap() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(new File("src/main/resources/testfile"), "rw"))
        {
            //get Channel
            FileChannel fileChannel = file.getChannel();
            //get mappedByteBuffer from fileChannel
            buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 1024 );
            // check buffer
            log.info("is Loaded in physical memory: {}",buffer.isLoaded());  //只是一个提醒而不是guarantee
            log.info("capacity {}",buffer.capacity());
            //write the content
            buffer.put(str.getBytes());
            log.info("str= {} currentThread={}", str, Thread.currentThread());
        }
    }

    @Test
    public void readWithMap() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(new File("src/main/resources/testfile"), "r"))
        {
            //get Channel
            FileChannel fileChannel = file.getChannel();
            //get mappedByteBuffer from fileChannel
            buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
            // check buffer
            log.info("is Loaded in physical memory: {}",buffer.isLoaded());  //只是一个提醒而不是guarantee
            log.info("capacity {}",buffer.capacity());
            byte[] bytes = new byte[size];

            //read the buffer
            for (int i = 0; i < bytes.length; i++)
            {
                bytes[i] = buffer.get();
//                log.info("get {}", buffer.get());
            }

            val str = new String(bytes);
            log.info("str= {} currentThread={}", str, Thread.currentThread());
        }
    }


}
