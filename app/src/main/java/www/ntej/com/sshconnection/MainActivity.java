package www.ntej.com.sshconnection;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {

    TextView tv_connection_result;
    Button btn_make_connection;
    ProgressBar progressBar;

    EditText ip0,ip1,ip2,ip3,user,port,password;

    String ipAddress,userName, sPortNumber,hostPassword ;

    SharedPreferences sharedPreferences;

    final static String PREF_FILE_NAME = "ssh_details";

    Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREF_FILE_NAME,0);


        tv_connection_result = (TextView)findViewById(R.id.tv_result);


        btn_make_connection = (Button)findViewById(R.id.btn_connection);

        progressBar = (ProgressBar)findViewById(R.id.progress_bar);


        ip0 = (EditText)findViewById(R.id.et_ipaddress_field1);
        ip1 = (EditText)findViewById(R.id.et_ipaddress_field2);
        ip2 = (EditText)findViewById(R.id.et_ipaddress_field3);
        ip3 = (EditText)findViewById(R.id.et_ipaddress_field4);
        user = (EditText)findViewById(R.id.et_user_name);
        port = (EditText)findViewById(R.id.et_portnumber);
        password = (EditText)findViewById(R.id.et_host_password);


       mToast = Toast.makeText(this,"Please enter all required fields",Toast.LENGTH_SHORT);


        btn_make_connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String ipField0,ipField1,ipField2,ipField3;

                ipField0 = ip0.getText().toString().trim();
                ipField1 = ip1.getText().toString().trim();
                ipField2 = ip2.getText().toString().trim();
                ipField3 = ip3.getText().toString().trim();

                userName = user.getText().toString().trim();
                sPortNumber = port.getText().toString().trim();
                hostPassword = password.getText().toString().trim();



                if(!ipField0.equals("")&&!ipField1.equals("")&&
                        !ipField2.equals("")&&!ipField3.equals("")&&!userName.equals("")&&
                        !sPortNumber.equals(""))
                {
                    //shared preferences
                    SharedPreferences.Editor editor =  sharedPreferences.edit();
                    editor.putString("ipfield0",ipField0);
                    editor.putString("ipfield1",ipField1);
                    editor.putString("ipfield2",ipField2);
                    editor.putString("ipfield3",ipField3);
                    editor.putString("username",userName);
                    editor.putString("portnumber",sPortNumber);
                    editor.putString("hostpassword",hostPassword);
                    editor.commit(); //committing

                    ipAddress = buildIpAddress(ipField0, ipField1, ipField2, ipField3);
                    Log.i("MyActivity", ipAddress);
                    new SSHConnector().execute(ipAddress, userName, sPortNumber, hostPassword);
                }
                else
                {
                    mToast.show();
                }

            }
        });

        fillDetailsFromSharedPreferences();

    }

    public String buildIpAddress(String ipField0,String ipField1,String ipField2,String ipField3)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(ipField0);
        builder.append(".");
        builder.append(ipField1);
        builder.append(".");
        builder.append(ipField2);
        builder.append(".");
        builder.append(ipField3);

        return builder.toString();
    }

    public void fillDetailsFromSharedPreferences()
    {
        if(sharedPreferences.contains("ipfield0")&&sharedPreferences.contains("ipfield1")&&
                sharedPreferences.contains("ipfield2")&&sharedPreferences.contains("ipfield3"))
        {
            ip0.setText(sharedPreferences.getString("ipfield0","0"));
            ip1.setText(sharedPreferences.getString("ipfield1","0"));
            ip2.setText(sharedPreferences.getString("ipfield2","0"));
            ip3.setText(sharedPreferences.getString("ipfield3","0"));
        }
        if(sharedPreferences.contains("username"))
        {
            user.setText(sharedPreferences.getString("username",""));
        }
        if(sharedPreferences.contains("portnumber"))
        {
            port.setText(sharedPreferences.getString("portnumber","00"));
        }
        if(sharedPreferences.contains("hostpassword"))
        {
            password.setText(sharedPreferences.getString("hostpassword",""));
        }
    }

    public class SSHConnector extends AsyncTask<String,Void,Boolean>
    {

        @Override
        protected void onPreExecute() {

            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            progressBar.setVisibility(View.INVISIBLE);

            if (aBoolean == true) {
                tv_connection_result.setText("Connected to RaspberryPi");

            } else {
                tv_connection_result.setText("Failed connecting to RaspberryPi");
            }

        }

        @Override
        protected Boolean doInBackground(String... hostDetails) {

            Session session;

            String ipAddress = hostDetails[0];
            String userName = hostDetails[1];
            String sPortNumber = hostDetails[2];
            int portNumber = Integer.parseInt(sPortNumber);
            String hostPassword = hostDetails[3];

                try {
                    JSch jSch = new JSch();

                    session = jSch.getSession(userName, ipAddress, portNumber);
                    session.setPassword(hostPassword);

                   // session = jSch.getSession("pi", "172.24.1.1", 22);
                    //session.setPassword("raspberry");

                    //avoid asking for key confirmation
                    Properties prop = new Properties();
                    prop.put("StrictHostKeyChecking", "no");
                    session.setConfig(prop);

                    session.connect();

                    //SSH Channel
                    ChannelExec channel = (ChannelExec) session.openChannel("exec");
                    BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
                    channel.setCommand("python ./light_sensor.py");
                    channel.connect();

                    String msg =null;
                    double i =0;
                    while ((msg = in.readLine())!=null)
                    {
                        Log.i("MyActivity","LDR_value("+i+"):"+msg);
                        i++;
                    }
                    // stops here infinite result

                    return true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return false;
                }

        }
    }
}
