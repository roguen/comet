//Copyright (c) 2015 Hitachi Data Systems, Inc.
//All Rights Reserved.
//
//   Licensed under the Apache License, Version 2.0 (the "License"); you may
//   not use this file except in compliance with the License. You may obtain
//   a copy of the License at
//
//         http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
//   WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
//   License for the specific language governing permissions and limitations
//   under the License.
//
//Package: Custom Object Metadata Enhancement Toolkit shared library
//Author: Chris Delezenski <chris.delezenski@hdsfed.com>
//Compilation Date: 2015-05-06
//License: Apache License, Version 2.0
//Version: 1.21.0
//(RPM) Release: 1
//SVN: r551+
package com.hdsfed.cometapi;

public class Coordinates {
	private String x="0",y="0";
	private String label;
	public Coordinates(String _label, String _x, String _y) {
		setLabel(_label);
		setX(_x);
		setY(_y);
	}
	public String getX() {
		return x;
	}
	public String getY() {
		return y;
	}
	public String getLabel() {
		return label;
	}
	public void setX(String _x) {
		x=_x;
	}
	public void setY(String _y) {
		y=_y;
	}
	public void setLabel(String _label) {
		label=_label;
	}
}
