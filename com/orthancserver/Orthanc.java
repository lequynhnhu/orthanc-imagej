/**
 * Orthanc - A Lightweight, RESTful DICOM Store
 * Copyright (C) 2012-2014 Medical Physics Department, CHU of Liege,
 * Belgium
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 **/


package com.orthancserver;

import ij.plugin.PlugIn;
import ij.Prefs;
import javax.swing.JOptionPane;

public class Orthanc implements PlugIn 
{
  public void run(String arg) 
  {
    SelectImageDialog d = new SelectImageDialog();
    d.Unserialize(Prefs.get("orthanc.servers", ""));

    boolean success = d.ShowModal();
    Prefs.set("orthanc.servers", d.Serialize());  

    if (success)
    {
      try 
      {
        DicomDecoder decoder = new DicomDecoder(d.GetSelectedConnection(), d.IsInstanceSelected(), d.GetSelectedUuid());
        decoder.GetImage().show();
      }
      catch (Exception e) 
      {
        JOptionPane.showMessageDialog(null, "Error while importing this image: " + e.getMessage());
        //e.printStackTrace(System.out);
      }
    }
  }
}
