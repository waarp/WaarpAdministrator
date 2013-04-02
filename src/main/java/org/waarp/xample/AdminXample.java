/**
   This file is part of Waarp Project.

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All Waarp Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Waarp is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Waarp .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.waarp.xample;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.swing.UIManager;

import org.waarp.xample.XAmple;

/**
 * XML configuration edition GUI helper
 * @author Frederic Bregier
 *
 */
public class AdminXample extends XAmple {

	private static final long serialVersionUID = 6020872788819087355L;

	public boolean stillLaunched = false;
	private List<AdminXample> list;
	
	public AdminXample(List<AdminXample> list) {
		super();
		this.list = list;
		stillLaunched = true;
		this.list.add(this);
	}

	public void exit() {
		if (!confirmation())
			return;
		saveRuntimeProperties();
		stillLaunched = false;
		this.list.remove(this);
		dispose();
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			if (confirmation())
			{
				saveRuntimeProperties();
				stillLaunched = false;
				this.list.remove(this);
				dispose();
			}
		}
	}

	public static AdminXample start(List<AdminXample> list)
	{
		assignDefaultFont();
		Properties props = new Properties();
		InputStream in = null;
		try {
			File file = new File(FILE_RUNTIME);
			if (file.exists())
			{
				in = new FileInputStream(file);
				props.load(in);
			}
			String lfName = props.getProperty(LOOK_AND_FEEL);
			String lfClassName = null;
			UIManager.LookAndFeelInfo[] lfi = UIManager.getInstalledLookAndFeels();
			for (int i = 0; i < lfi.length && lfClassName == null; i++)
			{
				if (lfi[i].getName().equals(lfName))
					lfClassName = lfi[i].getClassName();
			}
			if (lfClassName != null)
				UIManager.setLookAndFeel(lfClassName);
			else
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception ex) {
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception ex1) {
				ex1.printStackTrace();
			}
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception ignore) {
			}
		}
		AdminXample frame = new AdminXample(list);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		try {
			Rectangle r = new Rectangle();
			r.x = Integer.parseInt(props.getProperty(BOUNDS_LEFT));
			r.y = Integer.parseInt(props.getProperty(BOUNDS_TOP));
			r.width = Integer.parseInt(props.getProperty(BOUNDS_WIDTH));
			r.height = Integer.parseInt(props.getProperty(BOUNDS_HEIGHT));
			if (r.width > screenSize.width)
				r.width = screenSize.width;
			if (r.height > screenSize.height)
				r.height = screenSize.height;
			if (r.x + r.width > screenSize.width)
				r.x = screenSize.width - r.width;
			if (r.y + r.height > screenSize.height)
				r.y = screenSize.height - r.height;
			frame.setBounds(r.x, r.y, r.width, r.height);
			frame.validate();
		} catch (Exception ex) {
			frame.pack();
			Dimension d = frame.getSize();
			if (d.height > screenSize.height)
				d.height = screenSize.height;
			if (d.width > screenSize.width)
				d.width = screenSize.width;
			frame.setLocation((screenSize.width - d.width) / 2,
					(screenSize.height - d.height) / 2);
		}
		frame.setVisible(true);
		return frame;
	}

}
