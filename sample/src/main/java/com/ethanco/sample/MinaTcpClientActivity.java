package com.ethanco.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.ethanco.halo.turbo.Halo;
import com.ethanco.halo.turbo.ads.IHandlerAdapter;
import com.ethanco.halo.turbo.ads.ISession;
import com.ethanco.halo.turbo.impl.LogHandler;
import com.ethanco.halo.turbo.type.Mode;
import com.ethanco.sample.databinding.ActivityMinaTcpClientBinding;

public class MinaTcpClientActivity extends AppCompatActivity {

    private static final String TAG = "Z-MinaTcpClientActivity";
    private ActivityMinaTcpClientBinding binding;
    private ISession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_mina_tcp_client);

        binding.btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String targetIP = binding.etTargetIp.getText().toString();
                if (TextUtils.isEmpty(targetIP)) {
                    Toast.makeText(MinaTcpClientActivity.this, "目标IP不能为空", Toast.LENGTH_SHORT).show();
                }

                new Thread() {
                    @Override
                    public void run() {
                        Halo halo = new Halo.Builder()
                                .setMode(Mode.MINA_NIO_TCP_CLIENT)
                                .setBufferSize(2048)
                                .setTargetIP(targetIP)
                                .setTargetPort(19701)
                                .addHandler(new LogHandler(TAG))
                                .addHandler(new DemoHandler())
                                .build();

                        final boolean startSuccess = halo.start();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String connectResult = startSuccess ? "连接成功" : "连接失败";
                                binding.tvInfo.append(connectResult + "\r\n");
                            }
                        });
                    }
                }.start();
            }
        });

        binding.btnSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (session == null) {
                    Toast.makeText(MinaTcpClientActivity.this, "未建立连接", Toast.LENGTH_SHORT).show();
                } else {
                    session.write("hello，这是从Client发送的数据");
                }
            }
        });
    }

    class DemoHandler extends IHandlerAdapter {

        @Override
        public void sessionOpened(final ISession session) {
            MinaTcpClientActivity.this.session = session;
            //session.write("hello aaabbb");
        }

        @Override
        public void messageReceived(ISession session, Object message) {
            if (message instanceof String) {
                String receive = String.valueOf(message);
                binding.tvInfo.append("接收:" + receive + "\r\n");
            }
        }

        @Override
        public void messageSent(ISession session, Object message) {
            super.messageSent(session, message);
            if (message instanceof String) {
                String sendData = String.valueOf(message);
                binding.tvInfo.append("发送:" + sendData + "\r\n");
            }
        }
    }
}