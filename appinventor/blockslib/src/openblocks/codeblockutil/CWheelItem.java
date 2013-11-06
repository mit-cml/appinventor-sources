// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

public class CWheelItem extends JPanel{
	private static final long serialVersionUID = 328149080241L;
	public CWheelItem(){
		super(new BorderLayout());
		this.setPreferredSize(new Dimension(100,100));
	}
}
