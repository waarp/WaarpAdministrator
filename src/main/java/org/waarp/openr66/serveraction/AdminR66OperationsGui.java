/**
 * This file is part of Waarp Project.
 * 
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the
 * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
 * 
 * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Waarp . If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.waarp.openr66.serveraction;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.SoftBevelBorder;

import org.waarp.administrator.AdminGui;
import org.waarp.common.database.exception.WaarpDatabaseNoConnectionException;
import org.waarp.common.database.exception.WaarpDatabaseSqlException;
import org.waarp.common.digest.FilesystemBasedDigest;
import org.waarp.openr66.client.DirectTransfer;
import org.waarp.openr66.context.ErrorCode;
import org.waarp.openr66.context.R66FiniteDualStates;
import org.waarp.openr66.context.R66Result;
import org.waarp.openr66.database.DbConstant;
import org.waarp.openr66.database.data.DbHostAuth;
import org.waarp.openr66.protocol.configuration.Configuration;
import org.waarp.openr66.protocol.exception.OpenR66ProtocolPacketException;
import org.waarp.openr66.protocol.localhandler.LocalChannelReference;
import org.waarp.openr66.protocol.localhandler.packet.LocalPacketFactory;
import org.waarp.openr66.protocol.localhandler.packet.ShutdownPacket;
import org.waarp.openr66.protocol.localhandler.packet.ValidPacket;
import org.waarp.openr66.protocol.utils.ChannelUtils;
import org.waarp.openr66.protocol.utils.R66Future;
import org.waarp.openr66.server.ChangeBandwidthLimits;
import org.waarp.openr66.server.ConfigExport;
import org.waarp.openr66.server.ConfigImport;
import org.waarp.openr66.server.LogExport;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import java.awt.Dimension;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.SocketAddress;
import java.sql.Timestamp;
import java.util.Date;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import java.awt.Rectangle;
import java.awt.Cursor;

/**
 * @author "Frederic Bregier"
 * 
 */
public class AdminR66OperationsGui extends JFrame {
	public static AdminR66OperationsGui window;

	private static final long serialVersionUID = -7289307852740863337L;
	private JFrame adminGui;
	private AdminR66OperationsGui myself = this;
	private JSplitPane mainPanel;
	JProgressBar progressBarTransfer;
	R66Dialog dialog;

	/**
	 * @throws HeadlessException
	 */
	public AdminR66OperationsGui(JFrame adminGui) throws HeadlessException {
		super("Admin R66 Operations GUI");
		setMinimumSize(new Dimension(1000, 500));
		setPreferredSize(new Dimension(1000, 600));
		this.adminGui = adminGui;
		mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		mainPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent arg0) {
				mainPanel.setDividerLocation(mainPanel.getHeight() / 3);
			}
		});
		mainPanel.setDividerSize(2);
		mainPanel.setAutoscrolls(true);
		mainPanel.setBorder(new CompoundBorder());
		mainPanel.setOneTouchExpandable(true);
		setContentPane(mainPanel);
		initializePanel();
	}

	private void initializePanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(50, 250));
		buttonPanel.setMinimumSize(new Dimension(50, 250));
		GridBagLayout buttons = new GridBagLayout();
		buttons.columnWidths = new int[] { 194, 124, 0, 0, 0 };
		buttons.rowHeights = new int[] { 0, 0, 0, 0 };
		buttons.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		buttons.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
		buttonPanel.setLayout(buttons);
		mainPanel.setBottomComponent(buttonPanel);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setMinimumSize(new Dimension(5, 300));
		tabbedPane.setPreferredSize(new Dimension(5, 300));
		mainPanel.setTopComponent(tabbedPane);

		scrollPane_1 = new JScrollPane();
		scrollPane_1
				.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.weighty = 1.0;
		gbc_scrollPane_1.weightx = 1.0;
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridwidth = 5;
		// gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 0;
		buttonPanel.add(scrollPane_1, gbc_scrollPane_1);

		textFieldStatus = new JEditorPane();
		textFieldStatus.setToolTipText("Result of last command");
		scrollPane_1.setViewportView(textFieldStatus);
		textFieldStatus.setForeground(Color.GRAY);
		textFieldStatus.setBackground(new Color(255, 255, 153));
		textFieldStatus.setContentType("text/html");
		textFieldStatus.setEditable(false);

		scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null,
				null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.weighty = 1.0;
		gbc_scrollPane.weightx = 1.0;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridwidth = 5;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		buttonPanel.add(scrollPane, gbc_scrollPane);

		textPaneLog = new JTextArea();
		scrollPane.setViewportView(textPaneLog);
		textPaneLog.setToolTipText("Output of internal commands of R66");
		textPaneLog.setEditable(false);

		System.setOut(new PrintStream(new JTextAreaOutputStream(textPaneLog)));

		try {
			comboBoxServer = new JComboBox(DbHostAuth.getAllHosts(null));
		} catch (WaarpDatabaseNoConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (WaarpDatabaseSqlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		comboBoxServer.setMinimumSize(new Dimension(28, 22));
		GridBagConstraints gbc_comboBoxServer = new GridBagConstraints();
		gbc_comboBoxServer.gridwidth = 2;
		gbc_comboBoxServer.insets = new Insets(0, 0, 5, 5);
		gbc_comboBoxServer.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxServer.gridx = 0;
		gbc_comboBoxServer.gridy = 2;
		buttonPanel.add(comboBoxServer, gbc_comboBoxServer);

		progressBarTransfer = new JProgressBar();
		progressBarTransfer.setPreferredSize(new Dimension(500, 14));
		GridBagConstraints gbc_pb = new GridBagConstraints();
		gbc_pb.gridwidth = 3;
		gbc_pb.insets = new Insets(0, 0, 0, 5);
		gbc_pb.fill = GridBagConstraints.BOTH;
		gbc_pb.gridx = 0;
		gbc_pb.gridy = 3;
		buttonPanel.add(progressBarTransfer, gbc_pb);

		JButton btnCancel = new JButton("Close");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 3;
		gbc_btnCancel.gridy = 3;
		buttonPanel.add(btnCancel, gbc_btnCancel);
		progressBarTransfer.setVisible(false);

		initBandwidth(tabbedPane);
		initConfig(tabbedPane);
		initLog(tabbedPane);
		initShutdown(tabbedPane);
		mainPanel.setDividerLocation(200);
	}

	JButton btnGetBandwidthCurrent, btnGetConfigCurrent;
	JButton btnSetBandwidthConfiguration, btnSetConfigConfiguration;
	JFormattedTextField globWriteLimit;
	JFormattedTextField globReadLimit;
	JFormattedTextField sessionWriteLimit;
	JFormattedTextField sessionReadLimit;
	JScrollPane scrollPane_1;
	JEditorPane textFieldStatus;
	JScrollPane scrollPane;
	JTextArea textPaneLog;
	private JCheckBox chckbxHosts;
	private JCheckBox chckbxRules;
	private JTextField textFieldHosts;
	private JTextField textFieldRules;
	private JCheckBox chckbxPurgeHosts;
	private JCheckBox chckbxPurgeRules;
	private JButton btnHostsFile;
	private JButton btnRulesFile;
	private JButton btnShutdown;
	private JPasswordField passwordField;
	private JLabel lblPassword;
	private JCheckBox chckbxPurge;
	private JCheckBox chckbxClean;
	private JTextField textFieldStart;
	private JTextField textFieldStop;
	private JButton btnExportLogs;
	private JTextField textFieldResult;
	private JTextField textRuleUsedToGet;
	private JTextField textRuleToPut;
	private JLabel lblDates;
	private JTextField textRuleToExportLog;
	private JSeparator separator;
	private JLabel lblRuleToGet;
	private JLabel lblRuleToPut;
	private JLabel lblRuleToExport;
	private JLabel lblNbIfNo;
	private JComboBox comboBoxServer;

	private void initBandwidth(JTabbedPane tabbedPane) {
		JPanel bandwidthPanel = new JPanel();
		tabbedPane.addTab("Bandwidth management", null, bandwidthPanel, null);
		GridBagLayout gbl_xmlFilePanel = new GridBagLayout();
		gbl_xmlFilePanel.columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_xmlFilePanel.rowHeights = new int[] { 1, 1, 0, 0 };
		gbl_xmlFilePanel.columnWeights = new double[] { 0.0, 0.0, 1.0, 1.0, 1.0, 1.0 };
		gbl_xmlFilePanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
		bandwidthPanel.setLayout(gbl_xmlFilePanel);
		{
			btnGetBandwidthCurrent = new JButton("Get Bandwidth current configuration");
			btnGetBandwidthCurrent.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					R66AdminGuiActions action = new R66AdminGuiActions(
							R66AdminGuiActions.BANDWIDTHGET);
					action.execute();
				}
			});
			GridBagConstraints gbc_btnGetBandwidthCurrent = new GridBagConstraints();
			gbc_btnGetBandwidthCurrent.gridwidth = 2;
			gbc_btnGetBandwidthCurrent.insets = new Insets(0, 0, 5, 5);
			gbc_btnGetBandwidthCurrent.gridx = 4;
			gbc_btnGetBandwidthCurrent.gridy = 0;
			bandwidthPanel.add(btnGetBandwidthCurrent, gbc_btnGetBandwidthCurrent);
		}
		{
			JLabel lblGlobalLimit = new JLabel("Global Limit");
			GridBagConstraints gbc_lblGlobalLimit = new GridBagConstraints();
			gbc_lblGlobalLimit.insets = new Insets(0, 0, 5, 5);
			gbc_lblGlobalLimit.gridx = 0;
			gbc_lblGlobalLimit.gridy = 2;
			bandwidthPanel.add(lblGlobalLimit, gbc_lblGlobalLimit);
		}
		{
			JLabel lblWrite = new JLabel("Write");
			GridBagConstraints gbc_lblWrite = new GridBagConstraints();
			gbc_lblWrite.insets = new Insets(0, 0, 5, 5);
			gbc_lblWrite.anchor = GridBagConstraints.EAST;
			gbc_lblWrite.gridx = 1;
			gbc_lblWrite.gridy = 2;
			bandwidthPanel.add(lblWrite, gbc_lblWrite);
		}
		{
			globWriteLimit = new JFormattedTextField();
			globWriteLimit.setMinimumSize(new Dimension(100, 20));
			globWriteLimit.setValue(new Long(0));
			GridBagConstraints gbc_globWriteLimit = new GridBagConstraints();
			gbc_globWriteLimit.insets = new Insets(0, 0, 5, 5);
			gbc_globWriteLimit.fill = GridBagConstraints.HORIZONTAL;
			gbc_globWriteLimit.gridx = 2;
			gbc_globWriteLimit.gridy = 2;
			bandwidthPanel.add(globWriteLimit, gbc_globWriteLimit);
		}
		{
			JLabel lblRead = new JLabel("Read");
			GridBagConstraints gbc_lblRead = new GridBagConstraints();
			gbc_lblRead.insets = new Insets(0, 0, 5, 5);
			gbc_lblRead.anchor = GridBagConstraints.EAST;
			gbc_lblRead.gridx = 3;
			gbc_lblRead.gridy = 2;
			bandwidthPanel.add(lblRead, gbc_lblRead);
		}
		{
			globReadLimit = new JFormattedTextField();
			globReadLimit.setMinimumSize(new Dimension(100, 20));
			globReadLimit.setValue(new Long(0));
			GridBagConstraints gbc_globReadLimit = new GridBagConstraints();
			gbc_globReadLimit.insets = new Insets(0, 0, 5, 5);
			gbc_globReadLimit.fill = GridBagConstraints.HORIZONTAL;
			gbc_globReadLimit.gridx = 4;
			gbc_globReadLimit.gridy = 2;
			bandwidthPanel.add(globReadLimit, gbc_globReadLimit);
		}
		{
			JLabel lblSessionLimit = new JLabel("Session Limit");
			GridBagConstraints gbc_lblSessionLimit = new GridBagConstraints();
			gbc_lblSessionLimit.insets = new Insets(0, 0, 5, 5);
			gbc_lblSessionLimit.gridx = 0;
			gbc_lblSessionLimit.gridy = 3;
			bandwidthPanel.add(lblSessionLimit, gbc_lblSessionLimit);
		}
		{
			JLabel lblWrite_1 = new JLabel("Write");
			GridBagConstraints gbc_lblWrite_1 = new GridBagConstraints();
			gbc_lblWrite_1.anchor = GridBagConstraints.EAST;
			gbc_lblWrite_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblWrite_1.gridx = 1;
			gbc_lblWrite_1.gridy = 3;
			bandwidthPanel.add(lblWrite_1, gbc_lblWrite_1);
		}
		{
			sessionWriteLimit = new JFormattedTextField();
			sessionWriteLimit.setMinimumSize(new Dimension(100, 20));
			sessionWriteLimit.setValue(new Long(0));
			GridBagConstraints gbc_sessionWriteLimit = new GridBagConstraints();
			gbc_sessionWriteLimit.insets = new Insets(0, 0, 5, 5);
			gbc_sessionWriteLimit.fill = GridBagConstraints.HORIZONTAL;
			gbc_sessionWriteLimit.gridx = 2;
			gbc_sessionWriteLimit.gridy = 3;
			bandwidthPanel.add(sessionWriteLimit, gbc_sessionWriteLimit);
		}
		{
			JLabel lblRead_1 = new JLabel("Read");
			GridBagConstraints gbc_lblRead_1 = new GridBagConstraints();
			gbc_lblRead_1.anchor = GridBagConstraints.EAST;
			gbc_lblRead_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblRead_1.gridx = 3;
			gbc_lblRead_1.gridy = 3;
			bandwidthPanel.add(lblRead_1, gbc_lblRead_1);
		}
		{
			sessionReadLimit = new JFormattedTextField();
			sessionReadLimit.setMinimumSize(new Dimension(100, 20));
			sessionReadLimit.setValue(new Long(0));
			GridBagConstraints gbc_sessionReadLimit = new GridBagConstraints();
			gbc_sessionReadLimit.insets = new Insets(0, 0, 5, 5);
			gbc_sessionReadLimit.fill = GridBagConstraints.HORIZONTAL;
			gbc_sessionReadLimit.gridx = 4;
			gbc_sessionReadLimit.gridy = 3;
			bandwidthPanel.add(sessionReadLimit, gbc_sessionReadLimit);
		}
		{
			btnSetBandwidthConfiguration = new JButton("Set Bandwidth configuration");
			btnSetBandwidthConfiguration.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					R66AdminGuiActions action = new R66AdminGuiActions(
							R66AdminGuiActions.BANDWIDTHSET);
					action.execute();
				}
			});
			GridBagConstraints gbc_btnSetBandwidthConfiguration = new GridBagConstraints();
			gbc_btnSetBandwidthConfiguration.gridwidth = 2;
			gbc_btnSetBandwidthConfiguration.insets = new Insets(0, 0, 5, 5);
			gbc_btnSetBandwidthConfiguration.gridx = 4;
			gbc_btnSetBandwidthConfiguration.gridy = 4;
			bandwidthPanel.add(btnSetBandwidthConfiguration, gbc_btnSetBandwidthConfiguration);
		}

	}

	private void initConfig(JTabbedPane tabbedPane) {
		JPanel configPanel = new JPanel();
		tabbedPane.addTab("Configuration management", null, configPanel, null);
		GridBagLayout gbl_toolsPanel = new GridBagLayout();
		gbl_toolsPanel.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_toolsPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_toolsPanel.columnWeights = new double[] { 0.0, 1.0, 1.0, 1.0 };
		gbl_toolsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		configPanel.setLayout(gbl_toolsPanel);
		{
			btnGetConfigCurrent = new JButton("Get current configuration");
			btnGetConfigCurrent.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					R66AdminGuiActions action = new R66AdminGuiActions(
							R66AdminGuiActions.CONFIGEXPORT);
					action.execute();
				}
			});
			{
				lblRuleToGet = new JLabel("Rule to Get");
				GridBagConstraints gbc_lblRuleToGet = new GridBagConstraints();
				gbc_lblRuleToGet.insets = new Insets(0, 0, 5, 5);
				gbc_lblRuleToGet.anchor = GridBagConstraints.EAST;
				gbc_lblRuleToGet.gridx = 0;
				gbc_lblRuleToGet.gridy = 4;
				configPanel.add(lblRuleToGet, gbc_lblRuleToGet);
			}
			{
				textRuleUsedToGet = new JTextField();
				GridBagConstraints gbc_textRuleUsedToGet = new GridBagConstraints();
				gbc_textRuleUsedToGet.fill = GridBagConstraints.HORIZONTAL;
				gbc_textRuleUsedToGet.insets = new Insets(0, 0, 5, 5);
				gbc_textRuleUsedToGet.gridx = 1;
				gbc_textRuleUsedToGet.gridy = 4;
				configPanel.add(textRuleUsedToGet, gbc_textRuleUsedToGet);
				textRuleUsedToGet.setColumns(10);
			}
			{
				chckbxHosts = new JCheckBox("Hosts");
				GridBagConstraints gbc_chckbxHosts = new GridBagConstraints();
				gbc_chckbxHosts.insets = new Insets(0, 0, 5, 5);
				gbc_chckbxHosts.gridx = 2;
				gbc_chckbxHosts.gridy = 4;
				configPanel.add(chckbxHosts, gbc_chckbxHosts);
			}
			{
				chckbxRules = new JCheckBox("Rules");
				GridBagConstraints gbc_chckbxRules = new GridBagConstraints();
				gbc_chckbxRules.insets = new Insets(0, 0, 5, 5);
				gbc_chckbxRules.gridx = 3;
				gbc_chckbxRules.gridy = 4;
				configPanel.add(chckbxRules, gbc_chckbxRules);
			}
			GridBagConstraints gbc_btnGetBandwidthCurrent = new GridBagConstraints();
			gbc_btnGetBandwidthCurrent.gridwidth = 2;
			gbc_btnGetBandwidthCurrent.insets = new Insets(0, 0, 5, 5);
			gbc_btnGetBandwidthCurrent.gridx = 4;
			gbc_btnGetBandwidthCurrent.gridy = 4;
			configPanel.add(btnGetConfigCurrent, gbc_btnGetBandwidthCurrent);
		}
		{
			btnHostsFile = new JButton("Hosts file");
			btnHostsFile.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					File result = openFile(textFieldHosts.getText(), "Choose Hosts file", "xml");
					if (result != null) {
						textFieldHosts.setText(result.getAbsolutePath());
					}
				}
			});
			{
				separator = new JSeparator();
				separator.setOpaque(true);
				separator.setForeground(Color.BLACK);
				separator.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				separator.setBounds(new Rectangle(0, 0, 2, 2));
				separator.setBackground(Color.DARK_GRAY);
				separator.setMinimumSize(new Dimension(800, 2));
				separator.setRequestFocusEnabled(false);
				separator.setSize(new Dimension(800, 2));
				separator.setPreferredSize(new Dimension(800, 2));
				separator.setFocusTraversalKeysEnabled(false);
				GridBagConstraints gbc_separator = new GridBagConstraints();
				gbc_separator.gridwidth = 6;
				gbc_separator.insets = new Insets(0, 0, 5, 5);
				gbc_separator.gridx = 0;
				gbc_separator.gridy = 5;
				configPanel.add(separator, gbc_separator);
			}
			GridBagConstraints gbc_btnHostsFile = new GridBagConstraints();
			gbc_btnHostsFile.insets = new Insets(0, 0, 5, 5);
			gbc_btnHostsFile.gridx = 1;
			gbc_btnHostsFile.gridy = 6;
			configPanel.add(btnHostsFile, gbc_btnHostsFile);
		}
		{
			textFieldHosts = new JTextField();
			GridBagConstraints gbc_textFieldHosts = new GridBagConstraints();
			gbc_textFieldHosts.insets = new Insets(0, 0, 5, 5);
			gbc_textFieldHosts.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldHosts.gridx = 2;
			gbc_textFieldHosts.gridy = 6;
			configPanel.add(textFieldHosts, gbc_textFieldHosts);
			textFieldHosts.setColumns(10);
		}
		{
			textFieldRules = new JTextField();
			GridBagConstraints gbc_textFieldRules = new GridBagConstraints();
			gbc_textFieldRules.insets = new Insets(0, 0, 5, 5);
			gbc_textFieldRules.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldRules.gridx = 3;
			gbc_textFieldRules.gridy = 6;
			configPanel.add(textFieldRules, gbc_textFieldRules);
			textFieldRules.setColumns(10);
		}
		{
			btnRulesFile = new JButton("Rules file");
			btnRulesFile.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					File result = openFile(textFieldRules.getText(), "Choose Rules file", "xml");
					if (result != null) {
						textFieldRules.setText(result.getAbsolutePath());
					}
				}
			});
			GridBagConstraints gbc_btnRulesFile = new GridBagConstraints();
			gbc_btnRulesFile.insets = new Insets(0, 0, 5, 5);
			gbc_btnRulesFile.gridx = 4;
			gbc_btnRulesFile.gridy = 6;
			configPanel.add(btnRulesFile, gbc_btnRulesFile);
		}
		{
			lblRuleToPut = new JLabel("Rule to Put");
			GridBagConstraints gbc_lblRuleToPut = new GridBagConstraints();
			gbc_lblRuleToPut.insets = new Insets(0, 0, 5, 5);
			gbc_lblRuleToPut.anchor = GridBagConstraints.EAST;
			gbc_lblRuleToPut.gridx = 0;
			gbc_lblRuleToPut.gridy = 7;
			configPanel.add(lblRuleToPut, gbc_lblRuleToPut);
		}
		{
			textRuleToPut = new JTextField();
			GridBagConstraints gbc_textRuleToPut = new GridBagConstraints();
			gbc_textRuleToPut.insets = new Insets(0, 0, 5, 5);
			gbc_textRuleToPut.fill = GridBagConstraints.HORIZONTAL;
			gbc_textRuleToPut.gridx = 1;
			gbc_textRuleToPut.gridy = 7;
			configPanel.add(textRuleToPut, gbc_textRuleToPut);
			textRuleToPut.setColumns(10);
		}
		{
			chckbxPurgeHosts = new JCheckBox("Purge Hosts");
			GridBagConstraints gbc_chckbxPurgeHosts = new GridBagConstraints();
			gbc_chckbxPurgeHosts.insets = new Insets(0, 0, 5, 5);
			gbc_chckbxPurgeHosts.gridx = 2;
			gbc_chckbxPurgeHosts.gridy = 7;
			configPanel.add(chckbxPurgeHosts, gbc_chckbxPurgeHosts);
		}
		{
			chckbxPurgeRules = new JCheckBox("Purge Rules");
			GridBagConstraints gbc_chckbxPurgeRules = new GridBagConstraints();
			gbc_chckbxPurgeRules.insets = new Insets(0, 0, 5, 5);
			gbc_chckbxPurgeRules.gridx = 3;
			gbc_chckbxPurgeRules.gridy = 7;
			configPanel.add(chckbxPurgeRules, gbc_chckbxPurgeRules);
		}

		btnSetConfigConfiguration = new JButton("Set configuration");
		btnSetConfigConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				R66AdminGuiActions action = new R66AdminGuiActions(
						R66AdminGuiActions.CONFIGIMPORT);
				action.execute();
			}
		});
		GridBagConstraints gbc_btnSetBandwidthConfiguration = new GridBagConstraints();
		gbc_btnSetBandwidthConfiguration.gridwidth = 2;
		gbc_btnSetBandwidthConfiguration.insets = new Insets(0, 0, 5, 5);
		gbc_btnSetBandwidthConfiguration.gridx = 4;
		gbc_btnSetBandwidthConfiguration.gridy = 7;
		configPanel.add(btnSetConfigConfiguration, gbc_btnSetBandwidthConfiguration);

	}

	private void initLog(JTabbedPane tabbedPane) {
		JPanel logPanel = new JPanel();
		tabbedPane.addTab("Log management", null, logPanel, null);
		GridBagLayout gbl_toolsPanel = new GridBagLayout();
		gbl_toolsPanel.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_toolsPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_toolsPanel.columnWeights = new double[] { 0.0, 0.0, 1.0, 1.0 };
		gbl_toolsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		logPanel.setLayout(gbl_toolsPanel);

		{
			chckbxPurge = new JCheckBox("Purge");
			GridBagConstraints gbc_chckbxPurge = new GridBagConstraints();
			gbc_chckbxPurge.insets = new Insets(0, 0, 5, 5);
			gbc_chckbxPurge.gridx = 1;
			gbc_chckbxPurge.gridy = 4;
			logPanel.add(chckbxPurge, gbc_chckbxPurge);
		}
		{
			chckbxClean = new JCheckBox("Clean");
			GridBagConstraints gbc_chckbxClean = new GridBagConstraints();
			gbc_chckbxClean.insets = new Insets(0, 0, 5, 5);
			gbc_chckbxClean.gridx = 2;
			gbc_chckbxClean.gridy = 4;
			logPanel.add(chckbxClean, gbc_chckbxClean);
		}
		{
			lblNbIfNo = new JLabel(
					"NB: if no dates specified, all before yesterday midnight \r\n; Date format : yyyyMMddHHmmssSSS (completed on right side by '0')");
			lblNbIfNo.setPreferredSize(new Dimension(601, 30));
			lblNbIfNo.setFocusTraversalKeysEnabled(false);
			lblNbIfNo.setFocusable(false);
			lblNbIfNo.setAutoscrolls(true);
			GridBagConstraints gbc_lblNbIfNo = new GridBagConstraints();
			gbc_lblNbIfNo.gridwidth = 2;
			gbc_lblNbIfNo.insets = new Insets(0, 0, 5, 5);
			gbc_lblNbIfNo.gridx = 3;
			gbc_lblNbIfNo.gridy = 4;
			logPanel.add(lblNbIfNo, gbc_lblNbIfNo);
		}
		{
			lblDates = new JLabel("Dates");
			GridBagConstraints gbc_lblDates = new GridBagConstraints();
			gbc_lblDates.insets = new Insets(0, 0, 5, 5);
			gbc_lblDates.anchor = GridBagConstraints.EAST;
			gbc_lblDates.gridx = 1;
			gbc_lblDates.gridy = 5;
			logPanel.add(lblDates, gbc_lblDates);
		}
		{
			textFieldStart = new JTextField();
			GridBagConstraints gbc_textFieldStart = new GridBagConstraints();
			gbc_textFieldStart.insets = new Insets(0, 0, 5, 5);
			gbc_textFieldStart.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldStart.gridx = 2;
			gbc_textFieldStart.gridy = 5;
			logPanel.add(textFieldStart, gbc_textFieldStart);
			textFieldStart.setColumns(10);
		}
		{
			textFieldStop = new JTextField();
			GridBagConstraints gbc_textFieldStop = new GridBagConstraints();
			gbc_textFieldStop.insets = new Insets(0, 0, 5, 5);
			gbc_textFieldStop.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldStop.gridx = 3;
			gbc_textFieldStop.gridy = 5;
			logPanel.add(textFieldStop, gbc_textFieldStop);
			textFieldStop.setColumns(10);
		}
		{
			btnExportLogs = new JButton("Export Logs");
			btnExportLogs.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					R66AdminGuiActions action = new R66AdminGuiActions(
							R66AdminGuiActions.LOGEXPORT);
					action.execute();
				}
			});
			{
				lblRuleToExport = new JLabel("Rule to Export");
				GridBagConstraints gbc_lblRuleToExport = new GridBagConstraints();
				gbc_lblRuleToExport.insets = new Insets(0, 0, 5, 5);
				gbc_lblRuleToExport.anchor = GridBagConstraints.EAST;
				gbc_lblRuleToExport.gridx = 1;
				gbc_lblRuleToExport.gridy = 6;
				logPanel.add(lblRuleToExport, gbc_lblRuleToExport);
			}
			{
				textRuleToExportLog = new JTextField();
				GridBagConstraints gbc_textRuleToExportLog = new GridBagConstraints();
				gbc_textRuleToExportLog.insets = new Insets(0, 0, 5, 5);
				gbc_textRuleToExportLog.fill = GridBagConstraints.HORIZONTAL;
				gbc_textRuleToExportLog.gridx = 2;
				gbc_textRuleToExportLog.gridy = 6;
				logPanel.add(textRuleToExportLog, gbc_textRuleToExportLog);
				textRuleToExportLog.setColumns(10);
			}
			GridBagConstraints gbc_btnExportLogs = new GridBagConstraints();
			gbc_btnExportLogs.insets = new Insets(0, 0, 5, 5);
			gbc_btnExportLogs.gridx = 4;
			gbc_btnExportLogs.gridy = 6;
			logPanel.add(btnExportLogs, gbc_btnExportLogs);
		}
		{
			textFieldResult = new JTextField();
			textFieldResult.setEditable(false);
			GridBagConstraints gbc_textFieldResult = new GridBagConstraints();
			gbc_textFieldResult.gridwidth = 2;
			gbc_textFieldResult.insets = new Insets(0, 0, 5, 5);
			gbc_textFieldResult.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldResult.gridx = 2;
			gbc_textFieldResult.gridy = 7;
			logPanel.add(textFieldResult, gbc_textFieldResult);
			textFieldResult.setColumns(10);
		}

	}

	private void initShutdown(JTabbedPane tabbedPane) {
		JPanel shutdownPanel = new JPanel();
		tabbedPane.addTab("Shutdown servers", null, shutdownPanel, null);
		GridBagLayout gbl_toolsPanel = new GridBagLayout();
		gbl_toolsPanel.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_toolsPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_toolsPanel.columnWeights = new double[] { 0.0, 0.0, 1.0, 1.0 };
		gbl_toolsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		shutdownPanel.setLayout(gbl_toolsPanel);

		{
			btnShutdown = new JButton("Shutdown");
			btnShutdown.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					R66AdminGuiActions action = new R66AdminGuiActions(
							R66AdminGuiActions.SHUTDOWN);
					action.execute();
				}
			});
			{
				lblPassword = new JLabel("Password");
				lblPassword.setMinimumSize(new Dimension(60, 14));
				lblPassword.setMaximumSize(new Dimension(60, 14));
				GridBagConstraints gbc_lblPassword = new GridBagConstraints();
				gbc_lblPassword.anchor = GridBagConstraints.EAST;
				gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
				gbc_lblPassword.gridx = 1;
				gbc_lblPassword.gridy = 4;
				shutdownPanel.add(lblPassword, gbc_lblPassword);
			}
			{
				passwordField = new JPasswordField();
				GridBagConstraints gbc_passwordField = new GridBagConstraints();
				gbc_passwordField.insets = new Insets(0, 0, 5, 5);
				gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
				gbc_passwordField.gridx = 2;
				gbc_passwordField.gridy = 4;
				shutdownPanel.add(passwordField, gbc_passwordField);
			}
			GridBagConstraints gbc_btnShutdown = new GridBagConstraints();
			gbc_btnShutdown.insets = new Insets(0, 0, 5, 5);
			gbc_btnShutdown.gridx = 3;
			gbc_btnShutdown.gridy = 4;
			shutdownPanel.add(btnShutdown, gbc_btnShutdown);
		}

	}

	private void close() {
		this.adminGui.setEnabled(true);
		this.adminGui.requestFocus();
		this.setVisible(false);
	}

	/**
	 * @author Frederic Bregier
	 * 
	 */
	public class R66AdminGuiActions extends SwingWorker<String, Integer> {
		static final int BANDWIDTHGET = 1;
		static final int BANDWIDTHSET = 2;
		static final int CONFIGEXPORT = 3;
		static final int CONFIGIMPORT = 4;
		static final int LOGEXPORT = 5;
		static final int SHUTDOWN = 6;

		int method;

		R66AdminGuiActions(int method) {
			this.method = method;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected String doInBackground() throws Exception {
			disableAllButtons();
			startRequest();
			switch (method) {
				case BANDWIDTHGET:
					getBandwidth();
					break;
				case BANDWIDTHSET:
					setBandwidth();
					break;
				case CONFIGEXPORT:
					getConfig();
					break;
				case CONFIGIMPORT:
					setConfig();
					break;
				case LOGEXPORT:
					exportLog();
					break;
				case SHUTDOWN:
					shutdown();
					break;
				default:
					AdminGui.environnement.GuiResultat = "Action not recognized";
			}
			setStatus(AdminGui.environnement.GuiResultat);
			showDialog();
			stopRequest();
			return AdminGui.environnement.GuiResultat;
		}
	}

	private File openFile(String currentValue, String text, String extension) {
		JFileChooser chooser = null;
		if (currentValue != null) {
			String file = currentValue;
			if (file != null) {
				File ffile = new File(file).getParentFile();
				chooser = new JFileChooser(ffile);
			}
		}
		if (chooser == null) {
			chooser = new JFileChooser(System.getProperty("user.dir"));
		}
		if (extension != null) {
			FileExtensionFilter filter = new FileExtensionFilter(extension, text);
			chooser.setFileFilter(filter);
		}
		chooser.setDialogTitle(text);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}

	private void showDialog() {
		disableAllButtons();
		if (dialog != null) {
			dialog.dispose();
			dialog = null;
		}
		if (dialog == null) {
			dialog = new R66Dialog();
			dialog.setLocationRelativeTo(myself);
			if (dialog.isAlwaysOnTopSupported()) {
				dialog.setAlwaysOnTop(true);
			} else {
				dialog.toFront();
			}
		}
		dialog.textPaneDialog.setText(AdminGui.environnement.GuiResultat);
		dialog.setVisible(true);
		dialog.requestFocus();
	}

	private void setStatus(String mesg) {
		textFieldStatus.setText(mesg);
	}

	private void startRequest() {
		progressBarTransfer.setIndeterminate(true);
		progressBarTransfer.setValue(0);
		progressBarTransfer.setVisible(true);
		textPaneLog.setText("");
	}

	private void stopRequest() {
		progressBarTransfer.setIndeterminate(true);
		progressBarTransfer.setValue(0);
		progressBarTransfer.setVisible(false);
		myself.toFront();
		myself.requestFocus();
	}

	public void disableAllButtons() {
		// frmRClientGui.setEnabled(false);
		comboBoxServer.setEnabled(false);
		btnGetBandwidthCurrent.setEnabled(false);
		btnSetBandwidthConfiguration.setEnabled(false);
	}

	public void enableAllButtons() {
		// frmRClientGui.setEnabled(true);
		//System.err.println("Versions: "+Configuration.configuration.versions);
		try {
			int idx = comboBoxServer.getSelectedIndex();
			comboBoxServer.removeAllItems();
			for (DbHostAuth auth : DbHostAuth.getAllHosts(null)) {
				//System.err.println("Add: "+auth.toString());
				comboBoxServer.addItem(auth);
			}
			comboBoxServer.setSelectedIndex(idx);
		} catch (WaarpDatabaseNoConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (WaarpDatabaseSqlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		comboBoxServer.setEnabled(true);
		btnGetBandwidthCurrent.setEnabled(true);
		btnSetBandwidthConfiguration.setEnabled(true);
		myself.toFront();
	}

	public void getBandwidth() {
		DbHostAuth host = (DbHostAuth) comboBoxServer.getSelectedItem();
		if (host == null) {
			AdminGui.environnement.GuiResultat = "No Host selected!";
			return;
		}
		long time1 = System.currentTimeMillis();
		R66Future future = new R66Future(true);
		ChangeBandwidthLimits bandwidthLimits =
				new ChangeBandwidthLimits(future, -1, -1, -1, -1,
						AdminGui.environnement.networkTransaction);
		bandwidthLimits.setHost(host);
		bandwidthLimits.run();
		future.awaitUninterruptibly();
		long time2 = System.currentTimeMillis();
		long delay = time2 - time1;
		R66Result result = future.getResult();
		String message = null;
		if (future.isSuccess()) {
			ValidPacket packet = (ValidPacket) result.other;
			if (packet != null) {
				String[] values = packet.getSheader().split(" ");
				Long gw = Long.parseLong(values[0]);
				Long gr = Long.parseLong(values[1]);
				Long sw = Long.parseLong(values[2]);
				Long sr = Long.parseLong(values[3]);
				globWriteLimit.setValue(gw);
				globReadLimit.setValue(gr);
				sessionWriteLimit.setValue(sw);
				sessionReadLimit.setValue(sr);
			}
			if (result.code == ErrorCode.Warning) {
				message = "WARNED on bandwidth:\n    " +
						(result.other != null ? ((ValidPacket) result.other).getSheader() :
								"no information given")
						+ "\n    delay: " + delay;
			} else {
				message = "SUCCESS on Bandwidth:\n    " +
						(result.other != null ? ((ValidPacket) result.other).getSheader() :
								"no information given")
						+ "\n    delay: " + delay;
			}
		} else {
			if (result.code == ErrorCode.Warning) {
				message = "Bandwidth is WARNED: " + future.getCause();
			} else {
				message = "Bandwidth in FAILURE: " + future.getCause();
			}
		}
		AdminGui.environnement.GuiResultat = message;
	}

	public void setBandwidth() {
		DbHostAuth host = (DbHostAuth) comboBoxServer.getSelectedItem();
		if (host == null) {
			AdminGui.environnement.GuiResultat = "No Host selected!";
			return;
		}
		long time1 = System.currentTimeMillis();
		R66Future future = new R66Future(true);
		ChangeBandwidthLimits bandwidthLimits =
				new ChangeBandwidthLimits(future, (Long) globWriteLimit.getValue(),
						(Long) globReadLimit.getValue(),
						(Long) sessionWriteLimit.getValue(),
						(Long) sessionReadLimit.getValue(),
						AdminGui.environnement.networkTransaction);
		bandwidthLimits.setHost(host);
		bandwidthLimits.run();
		future.awaitUninterruptibly();
		long time2 = System.currentTimeMillis();
		long delay = time2 - time1;
		R66Result result = future.getResult();
		String message = null;
		if (future.isSuccess()) {
			if (result.code == ErrorCode.Warning) {
				message = "WARNED on bandwidth:\n    " +
						(result.other != null ? ((ValidPacket) result.other).getSheader() :
								"no information given")
						+ "\n    delay: " + delay;
			} else {
				message = "SUCCESS on Bandwidth:\n    " +
						(result.other != null ? ((ValidPacket) result.other).getSheader() :
								"no information given")
						+ "\n    delay: " + delay;
			}
		} else {
			if (result.code == ErrorCode.Warning) {
				message = "Bandwidth is WARNED: " + future.getCause();
			} else {
				message = "Bandwidth in FAILURE: " + future.getCause();
			}
		}
		AdminGui.environnement.GuiResultat = message;
	}

	public void getConfig() {
		DbHostAuth host = (DbHostAuth) comboBoxServer.getSelectedItem();
		if (host == null) {
			AdminGui.environnement.GuiResultat = "No Host selected!";
			return;
		}
		long time1 = System.currentTimeMillis();
		R66Future future = new R66Future(true);
		boolean getHost = chckbxHosts.isSelected();
		boolean getRule = chckbxRules.isSelected();
		String ruleToGet = textRuleUsedToGet.getText();
		ConfigExport export = new ConfigExport(future, getHost, getRule,
				AdminGui.environnement.networkTransaction);
		export.setHost(host);
		export.run();
		future.awaitUninterruptibly();
		long time2 = System.currentTimeMillis();
		long delay = time2 - time1;
		R66Result result = future.getResult();
		String message = "";
		if (future.isSuccess()) {
			ValidPacket packet = (ValidPacket) result.other;
			if (packet != null) {
				String[] values = packet.getSheader().split(" ");
				String shost = values[0];
				String srule = null;
				if (values.length > 1) {
					srule = values[1];
				}
				if (ruleToGet == null || ruleToGet.isEmpty()) {
					message = "No rule passed to download configuration, so cannot get configuration";
				}
				if (message.length() > 1) {
					// error
					message = "Get Config in FAILURE: " + message;
				} else {
					// XXX FIXME should get config files
					if (getHost && shost != null && shost.length() > 1) {
						future = new R66Future(true);
						DirectTransfer transfer = new DirectTransfer(future, host.getHostid(), shost,
								ruleToGet, "Get Host Configuration from "
										+ AdminGui.environnement.hostId,
								AdminGui.environnement.isMD5, Configuration.configuration.BLOCKSIZE,
								DbConstant.ILLEGALVALUE,
								AdminGui.environnement.networkTransaction);
						transfer.run();
						if (future.isSuccess()) {
							R66Result resultHost = future.getResult();
							shost = resultHost.file.getTrueFile().getAbsolutePath();
							message += " Host file into: " + shost;
						} else {
							shost = "Cannot get Host file: " + shost;
							message += shost;
						}
					}
					if (getRule && srule != null && srule.length() > 1) {
						future = new R66Future(true);
						DirectTransfer transfer = new DirectTransfer(future, host.getHostid(), srule,
								ruleToGet, "Get Rule Configuration from "
										+ AdminGui.environnement.hostId,
								AdminGui.environnement.isMD5, Configuration.configuration.BLOCKSIZE,
								DbConstant.ILLEGALVALUE,
								AdminGui.environnement.networkTransaction);
						transfer.run();
						if (future.isSuccess()) {
							R66Result resultRule = future.getResult();
							srule = resultRule.file.getTrueFile().getAbsolutePath();
							message += " Ruke file into: " + srule;
						} else {
							srule = "Cannot get Rule file: " + srule;
							message += srule;
						}
					}
					// XXX FIXME then set downloaded file to text fields
					if (getHost) {
						textFieldHosts.setText(shost);
					}
					if (getRule) {
						textFieldRules.setText(srule);
					}
				}
			}
			if (result.code == ErrorCode.Warning) {
				message = "WARNED on Get Config:\n    "
						+
						(result.other != null ? ((ValidPacket) result.other).getSheader() + message
								:
								"no information given")
						+ "\n    delay: " + delay;
			} else {
				message = "SUCCESS on Get Config:\n    "
						+
						(result.other != null ? ((ValidPacket) result.other).getSheader() + message
								:
								"no information given")
						+ "\n    delay: " + delay;
			}
		} else {
			if (result.code == ErrorCode.Warning) {
				message = "Get Config is WARNED: " + future.getCause();
			} else {
				message = "Get Config in FAILURE: " + future.getCause();
			}
		}
		AdminGui.environnement.GuiResultat = message;
	}

	public void setConfig() {
		DbHostAuth host = (DbHostAuth) comboBoxServer.getSelectedItem();
		if (host == null) {
			AdminGui.environnement.GuiResultat = "No Host selected!";
			return;
		}
		long time1 = System.currentTimeMillis();
		R66Future future = new R66Future(true);
		String hostfile = textFieldHosts.getText();
		String rulefile = textFieldRules.getText();
		boolean erazeHost = chckbxPurgeHosts.isSelected();
		boolean erazeRule = chckbxPurgeRules.isSelected();
		String ruleToPut = textRuleToPut.getText();
		String error = "";
		String msg = "";
		// XXX FIXME should send config files first
		if ((hostfile == null || hostfile.isEmpty()) &&
			(rulefile == null || rulefile.isEmpty())) {
			error = "No rule file neither host file passed as argument, so cannot set configuration";
		}
		if (ruleToPut == null || ruleToPut.isEmpty()) {
			error = "No rule passed to upload configuration, so cannot set configuration";
		}
		String message = null;
		if (error.length() > 1) {
			// error
			message = "Get Config in FAILURE: " + error;
			AdminGui.environnement.GuiResultat = message;
			return;
		}
		if (hostfile != null && hostfile.length() > 1) {
			future = new R66Future(true);
			DirectTransfer transfer = new DirectTransfer(future, host.getHostid(), hostfile,
					ruleToPut, "Set Host Configuration from "
							+ AdminGui.environnement.hostId,
					AdminGui.environnement.isMD5, Configuration.configuration.BLOCKSIZE,
					DbConstant.ILLEGALVALUE,
					AdminGui.environnement.networkTransaction);
			transfer.run();
			if (!future.isSuccess()) {
				error = "Cannot set: " + hostfile;
			} else {
				msg += " Host Configuration transmitted";
			}
		}
		if (rulefile != null && rulefile.length() > 1) {
			future = new R66Future(true);
			DirectTransfer transfer = new DirectTransfer(future, host.getHostid(), rulefile,
					ruleToPut, "Set Rule Configuration from "
							+ AdminGui.environnement.hostId,
					AdminGui.environnement.isMD5, Configuration.configuration.BLOCKSIZE,
					DbConstant.ILLEGALVALUE,
					AdminGui.environnement.networkTransaction);
			transfer.run();
			if (!future.isSuccess()) {
				error += "&& Cannot Set: " + rulefile;
			} else {
				msg += " Rule Configuration transmitted";
			}
		}
		if (error.length() > 1) {
			// error
			message = "Get Config in FAILURE: " + error;
			AdminGui.environnement.GuiResultat = message;
			return;
		}
		ConfigImport importCmd = new ConfigImport(future, erazeHost, erazeRule,
				hostfile, rulefile,
				AdminGui.environnement.networkTransaction);
		importCmd.setHost(host);
		importCmd.run();
		future.awaitUninterruptibly();
		long time2 = System.currentTimeMillis();
		long delay = time2 - time1;
		R66Result result = future.getResult();
		if (future.isSuccess()) {
			if (result.code == ErrorCode.Warning) {
				message = "WARNED on Set Config:\n    " +
						(result.other != null ? ((ValidPacket) result.other).getSheader() + msg :
								msg)
						+ "\n    delay: " + delay;
			} else {
				message = "SUCCESS on Set Config:\n    " +
						(result.other != null ? ((ValidPacket) result.other).getSheader() + msg :
								msg)
						+ "\n    delay: " + delay;
			}
		} else {
			if (result.code == ErrorCode.Warning) {
				message = "Set Config is WARNED: " + future.getCause() + msg;
			} else {
				message = "Set Config in FAILURE: " + future.getCause();
			}
		}
		AdminGui.environnement.GuiResultat = message;
	}

	public void exportLog() {
		DbHostAuth host = (DbHostAuth) comboBoxServer.getSelectedItem();
		if (host == null) {
			AdminGui.environnement.GuiResultat = "No Host selected!";
			return;
		}
		long time1 = System.currentTimeMillis();
		boolean purgeLog = chckbxPurge.isSelected();
		boolean clean = chckbxClean.isSelected();
		Timestamp start = null;
		Timestamp stop = null;
		String sstart = textFieldStart.getText();
		String sstop = textFieldStop.getText();
		if (sstart != null) {
			start = LogExport.fixDate(sstart);
		}
		if (sstop != null) {
			stop = LogExport.fixDate(sstop, start);
		}
		if (start == null && stop == null) {
			stop = LogExport.getTodayMidnight();
		}
		if (start != null) {
			System.err.println("Start: " + (new Date(start.getTime())).toString());
		}
		if (stop != null) {
			System.err.println("Stop: " + (new Date(stop.getTime())).toString());
		}

		R66Future future = new R66Future(true);
		LogExport export = new LogExport(future, purgeLog, clean, start, stop,
				AdminGui.environnement.networkTransaction);
		export.setHost(host);
		export.run();
		future.awaitUninterruptibly();
		long time2 = System.currentTimeMillis();
		long delay = time2 - time1;
		R66Result result = future.getResult();
		String message = "";
		if (future.isSuccess()) {
			ValidPacket packet = (ValidPacket) result.other;
			if (packet != null) {
				String[] values = packet.getSheader().split(" ");
				String fileExported = values[0];
				textFieldResult.setText(fileExported);
				// XXX FIXME download logs
				if (fileExported != null && fileExported.length() > 1) {
					String ruleToExport = textRuleToExportLog.getText();
					if (ruleToExport == null || ruleToExport.isEmpty()) {
						message = "Cannot get: " + fileExported + " since no rule to export specified\n";
					} else {
						future = new R66Future(true);
						DirectTransfer transfer = new DirectTransfer(future, host.getHostid(),
								fileExported,
								ruleToExport, "Get Exported Logs from "
										+ AdminGui.environnement.hostId,
								AdminGui.environnement.isMD5, Configuration.configuration.BLOCKSIZE,
								DbConstant.ILLEGALVALUE,
								AdminGui.environnement.networkTransaction);
						transfer.run();
						if (!future.isSuccess()) {
							message = "Cannot get: " + fileExported + "\n";
						} else {
							textFieldResult.setText(future.getResult().file.getTrueFile()
									.getAbsolutePath());
						}
					}
				}
			}
			if (result.code == ErrorCode.Warning) {
				message += "\nWARNED on Export Logs:\n    " +
						(result.other != null ? ((ValidPacket) result.other).getSheader() :
								"no information given")
						+ "\n    delay: " + delay+"\n";
			} else {
				message += "\nSUCCESS on Export Logs:\n    " +
						(result.other != null ? ((ValidPacket) result.other).getSheader() :
								"no information given")
						+ "\n    delay: " + delay;
			}
		} else {
			if (result.code == ErrorCode.Warning) {
				message += "\nExport Logs is WARNED: " + future.getCause();
			} else {
				message += "\nExport Logs in FAILURE: " + future.getCause();
			}
		}
		AdminGui.environnement.GuiResultat = message;
	}

	public void shutdown() {
		DbHostAuth host = (DbHostAuth) comboBoxServer.getSelectedItem();
		if (host == null) {
			AdminGui.environnement.GuiResultat = "No Host selected!";
			return;
		}
		String skey = null;
		try {
			char[] pwd = passwordField.getPassword();
			if (pwd == null || pwd.length == 0) {
				AdminGui.environnement.GuiResultat = "No Password given!";
				return;
			}
			skey = new String(pwd);
		} catch (NullPointerException e) {
			AdminGui.environnement.GuiResultat = "No Password given!";
			return;
		}
		long time1 = System.currentTimeMillis();
		byte[] key;
		key = FilesystemBasedDigest.passwdCrypt(skey.getBytes());
		final ShutdownPacket packet = new ShutdownPacket(
				key);
		final SocketAddress socketServerAddress = host.getSocketAddress();
		LocalChannelReference localChannelReference = null;
		localChannelReference = AdminGui.environnement.networkTransaction
				.createConnectionWithRetry(socketServerAddress, host.isSsl(), null);
		String message = null;
		if (localChannelReference == null) {
			message = "Bandwidth in FAILURE: " + "Cannot connect to " + host.getSocketAddress();
			AdminGui.environnement.GuiResultat = message;
			return;
		}
		localChannelReference.sessionNewState(R66FiniteDualStates.SHUTDOWN);
		try {
			ChannelUtils.writeAbstractLocalPacket(localChannelReference, packet, false);
		} catch (OpenR66ProtocolPacketException e) {
			message = "Bandwidth in FAILURE: " + "Cannot send order to " + host.getSocketAddress()
					+ "[" + e.getMessage() + "]";
			AdminGui.environnement.GuiResultat = message;
			return;
		}
		localChannelReference.getFutureRequest().awaitUninterruptibly();
		R66Result result = localChannelReference.getFutureRequest()
				.getResult();
		if (localChannelReference.getFutureRequest().isSuccess()) {
			message = "SUCCESS on Shutdown OK";
		} else {
			if (result.other instanceof ValidPacket
					&&
					((ValidPacket) result.other).getTypeValid() == LocalPacketFactory.SHUTDOWNPACKET) {
				message = "SUCCESS on Shutdown command OK";
			} else if (result.code == ErrorCode.Shutdown) {
				message = "SUCCESS on Shutdown command done";
			} else {
				message = "FAILURE on Shutdown: " + result.toString() + "[" + localChannelReference
						.getFutureRequest().getCause() + "]";
			}
		}
		long time2 = System.currentTimeMillis();
		long delay = time2 - time1;
		message += "\n    delay: " + delay;
		AdminGui.environnement.GuiResultat = message;
	}

	public static class JTextAreaOutputStream extends OutputStream {
		JTextArea ta;

		public JTextAreaOutputStream(JTextArea t) {
			super();
			ta = t;
		}

		public void write(int i) {
			ta.append(Character.toString((char) i));
		}

		public void write(char[] buf, int off, int len) {
			String s = new String(buf, off, len);
			ta.append(s);
		}

	}

}
