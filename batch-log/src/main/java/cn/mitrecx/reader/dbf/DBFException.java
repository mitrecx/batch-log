/*
  DBFException
	Represents exceptions happen in the JAvaDBF classes.

  This file is part of JavaDBF packege.

  author: anil@linuxense.com
  license: LGPL (http://www.gnu.org/copyleft/lesser.html)

  $Id: DBFException.java,v 1.2 2004/03/31 10:40:18 anil Exp $
*/
package cn.mitrecx.reader.dbf;

import java.io.IOException;

@SuppressWarnings("serial")
public class DBFException extends IOException {

	public DBFException() {

		super();
	}

	public DBFException( String msg) {

		super( msg);
	}
}
