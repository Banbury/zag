
This is Zag, an implementation of glulx v3.1.2 for Java

Authors:
* Jon Zeppieri (see license/README-license.txt for details) [jalfred97 @ yahoo . com]
* David Turner <novalis@novalis.org> (unicode, heap, floats, misc fixes)
* Banbury 

-------------------------------------------------------

Contents:
1. Introduction
2. Requirements
3. Using Zag as a normal Java application
4. Glk
5. Features missing and present
6. Bugs

-------------------------------------------------------


1. Introduction:

Zag implements v3.1.2 of the Glulx standard and v.0.7.4 of the Glk specification.
  
http://www.eblong.com/zarf/glulx/index.html
http://www.eblong.com/zarf/glk/glk-spec-074.txt

You can download binary packages of Zag here:
 http://dl.bintray.com/banbury/maven

2. Requirements

Zag requires a JRE (Java Runtime Environment) or
or a JDK (Java Development Kit) version 1.7 or later.

3. Using Zag

Zag comes in three different packages, one for each platform. Download the
package for your operating system and unpack it somewhere your harddrive.
The folder within the package contains a file to launch Zag. On Windows
start 'zag.exe', on Linux use the shell script 'zag'. The Mac OSX package
contains an app, that can be started like a regular application. (I haven't
tested it, though)

Games can be loaded by using 'File/Open file' or by dragging and dropping
a file on the window.

There a two features, that can be configured with a property file.

https://raw.githubusercontent.com/Banbury/zag/refactoring/zag-swing/zag.properties

The Swing look&feel of Zag can be changed by setting the 'plaf' property.

  plaf=com.sun.java.swing.plaf.gtk.GTKLookAndFeel
  
This will change the look&feel to GTK.

Zag does now respect the ROM of Glulx games. An attempt to write to ROM will end the game
with an error. But some older (Superglús) games can no longer be played with this
behaviour. To turn it off, the property 'save_memory' has to be set to 'false'.

  save_memory=false 

4. Glk

Since Zag implements Glulx (and does not implement it trivially), it
supports the glk opcode, which, in turn, means that it comes with
an implementation of Glk.  This is Zing, which stands for "Zing is not Glk."  
For one thing, it makes no attempt
whatsoever to keep holy the full 32-bit address range of Glk.  Java
does not have unsigned integers, and it is difficult to
implement them usefully.  (You cannot, for instance, index an array
with a Java long integer.)  Additionally, Zing does not implement the
Glk dispatch system, since Java already supports interface discovery
through the reflection API.  (I did, however, need to support the
notion of "in parameters" and "out parameters," but I did this with
extra type information.)

Note that Zing does not support the interfaces proposed by Matthew
Russotto; it is not quite so faithful to the C API.

Zing, of course, comes with Zag, and is under the exact same license
(BSD), so you can use it for whatever purposes you desire.


5. Features missing and present

a. Missing: cut, copy and paste.  

The most notable feature lacking in Zag (if you do not count speed) is
the ability to select, cut, and paste text.  Zag (or rather Zing, the
Glk-alike library used by Zag) does not use the standard Java text
widgets for its story windows, so it does not get this ability "for
free," as it were.  

b. Present:  command history

Athough it lacks cut & paste, Zag does have a command history buffer
for story windows (a separate history for each such window).  Use the
up and down arrow keys to scroll backwards and forwards through the
history.  Note that this feature is only present on story windows, not
on "status," or grid, windows.

c: Present:  ability to load a file from a URL

Under the File menu, there is an option, "Load URL..."  If you select
this, a dialog will pop up prompting you to enter a URL.  You may, for
instance, enter the URL of a game file stored on the IF archive.  The
file will be downloaded and then opened.  The downloaded file will be
placed in temporary storage (e.g., under UNIX, it will be in the /tmp
directory; perhaps this should be configurable).  Please note that you
must enter a fully qualified URL, including the protocol string (e.g.,
http://).

Many files on the archive, however, are not directly readable by Zag.
Zag understands glulx (.ulx) files and blorb (.blb) files, but it will
not delve into a .zip file to extract either of these.

d. Present:  emacs key bindings (not all of them, of course)

In buffer (story) windows, Ctrl-a will position the cursor at the
start of the input.  Ctrl-e will position it at the end.
Ctrl-right-arrow will move the cursor forward one word;
Ctrl-left-arrow will move it back one word.  Meta-backspace will erase
the previous word.  (On a Mac, "Meta" is the Command, or Apple, key.
On Windows, it is the Alt key.)


6. Bugs

a. WindowMask (from the WinGlulxe configration file specification) is
implemented using an opaque, black background where it ought to be
transparent.  Java isn't up to the task yet.
