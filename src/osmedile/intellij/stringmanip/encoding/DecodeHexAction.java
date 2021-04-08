package osmedile.intellij.stringmanip.encoding;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import org.apache.commons.lang3.NotImplementedException;
import org.bouncycastle.util.encoders.Hex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import osmedile.intellij.stringmanip.AbstractStringManipAction;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.nio.charset.Charset;
import java.util.Map;

public class DecodeHexAction extends AbstractStringManipAction<Charset> {

	@NotNull
	@Override
	public Pair<Boolean, Charset> beforeWriteAction(Editor editor, DataContext dataContext) {
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(new String[]{
			"UTF-8",
			"ASCII",
			"CP1256",
			"ISO-8859-1",
			"ISO-8859-2",
			"ISO-8859-6",
			"ISO-8859-15",
			"Windows-1252"});
		JComboBox<String> charsetComboBox = new ComboBox<>(model, 20);
		charsetComboBox.setEditable(true);
		charsetComboBox.setOpaque(true);
		Color defaultColor = charsetComboBox.getForeground();
		charsetComboBox.setSelectedItem("UTF-8");
		final ComboBoxEditor comboBoxEditor = charsetComboBox.getEditor();
		final JTextComponent tc = (JTextComponent) comboBoxEditor.getEditorComponent();
		tc.getDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			protected void textChanged(DocumentEvent documentEvent) {
				JTextField editorJComp = (JTextField) comboBoxEditor.getEditorComponent();
				try {
					Charset.forName(getCharset(charsetComboBox));
					editorJComp.setForeground(defaultColor);
				} catch (Exception ee) {
					editorJComp.setForeground(JBColor.RED);
				}
			}
		});

		String dimensionServiceKey = getDimensionServiceKey();
		DialogWrapper dialogWrapper = new DialogWrapper(editor.getProject()) {
			{
				init();
				setTitle("Choose Charset");
			}

			@Nullable
			@Override
			public JComponent getPreferredFocusedComponent() {
				return charsetComboBox;
			}

			@Nullable
			@Override
			protected String getDimensionServiceKey() {
				return dimensionServiceKey;
			}

			@Nullable
			@Override
			protected JComponent createCenterPanel() {
				return charsetComboBox;
			}

			@Override
			protected void doOKAction() {
				super.doOKAction();
			}
		};

		boolean b = dialogWrapper.showAndGet();
		if (!b) {
			return stopExecution();
		}

		try {
			Charset charset = Charset.forName(getCharset(charsetComboBox));
			return continueExecution(charset);
		} catch (Exception e) {
			Messages.showErrorDialog(editor.getProject(), String.valueOf(e), "Invalid Charset");
			return stopExecution();
		}
	}

	@NotNull
	protected String getCharset(JComboBox<String> charsetComboBox) {
		ComboBoxEditor comboBoxEditor = charsetComboBox.getEditor();
		final JTextComponent tc = (JTextComponent) comboBoxEditor.getEditorComponent();
		return tc.getText().trim();
	}

	@Override
	protected String transformSelection(Editor editor, Map<String, Object> actionContext, DataContext dataContext, String s, Charset charset) {
		return new String(Hex.decode(s.getBytes(charset)), charset);
	}

	@Override
	public String transformByLine(Map<String, Object> actionContext, String s) {
		throw new NotImplementedException();
	}

	protected String getDimensionServiceKey() {
		return "StringManipulation.HexDecodingDialog";
	}
}
