package de.banbury.zag.tests

import java.nio.ByteBuffer

import org.p2c2e.zing.Fileref
import org.p2c2e.zing.IGlk
import org.p2c2e.zing.streams.FileStream
import org.p2c2e.zing.streams.Stream
import org.p2c2e.zing.types.StreamResult

import spock.lang.Specification

class FileStreamSpec extends Specification {

	def "creating a file stream"() {
		setup:
		Fileref fileref = Fileref.createTemp(IGlk.FILEUSAGE_DATA)
		Stream stream = new FileStream(fileref, IGlk.FILEMODE_READ_WRITE, false)

		expect:
		stream.wcount == 0
		stream.rcount == 0
		stream.canRead() == true
		stream.canWrite() == true

		cleanup:
		stream.close()
	}

	def "get single character"() {
		setup:
		Fileref fileref = createTempfile()
		Stream stream = new FileStream(fileref, IGlk.FILEMODE_READ, false)

		when:
		int c = stream.getChar()
		StreamResult sr = stream.close()

		then:
		((char)c) == 'T'
		sr.readcount == 1
		stream.rcount == sr.readcount
	}

	def "read line"() {
		setup:
		Fileref fileref = createTempfile()
		Stream stream = new FileStream(fileref, IGlk.FILEMODE_READ, false)
		ByteBuffer buf = ByteBuffer.wrap(new byte[256])

		when:
		int n = stream.getLine(buf, 256)
		StreamResult sr = stream.close()

		then:
		n == 46
		n == sr.readcount
		buf.array()[n] == 0
	}

	def "read line unicode"() {
		setup:
		Fileref fileref = createTempfileUnicode()
		Stream stream = new FileStream(fileref, IGlk.FILEMODE_READ, false)
		ByteBuffer buf = ByteBuffer.wrap(new byte[256])

		when:
		int n = stream.getLineUni(buf, 256)
		StreamResult sr = stream.close()

		then:
		n == 46
		n == sr.readcount
		buf.array()[n] == 0
	}

	def "get buffer"() {
		setup:
		Fileref fileref = createTempfile()
		Stream stream = new FileStream(fileref, IGlk.FILEMODE_READ, false)
		ByteBuffer buf = ByteBuffer.wrap(new byte[256])

		when:
		int n = stream.getBuffer(buf, 256)
		StreamResult sr = stream.close()

		then:
		n == 67
		sr.readcount == n
		buf.get(0) == "T"
	}

	def "get char unicode"() {
		setup:
		Fileref fileref = createTempfileUnicode()
		Stream stream = new FileStream(fileref, IGlk.FILEMODE_READ, false)

		when:
		int c = stream.getCharUni()
		stream.close()

		then:
		c == "T"
	}

	def "get buffer from unicode file"() {
		setup:
		Fileref fileref = createTempfileUnicodePutChar()
		Stream stream = new FileStream(fileref, IGlk.FILEMODE_READ, false)
		ByteBuffer buf = ByteBuffer.wrap(new byte[256])

		when:
		int n = stream.getBuffer(buf, 256)
		StreamResult sr = stream.close()

		then:
		n == 3
		((char)buf.get(0)) == "T"
	}

	def "read unicode data in char/binary mode"() {
		setup:
		File f = File.createTempFile("zag", ".glkdata", new File(System.getProperty("user.dir")))
		f.write("Test ${0x3B1 as char}x${0x201C as char}x${0x201D as char}.^Re${0xEB as char}l${0xEE as char}ty-${0xA9 as char}.^")
		Fileref fr = Fileref.createByName(Fileref.FILEMODE_READ, f.name)
		Stream stream = new FileStream(fr, IGlk.FILEMODE_READ, false)
		ByteBuffer buf = ByteBuffer.wrap(new byte[256])

		when:
		int n = stream.getBuffer(buf, 256)
		StreamResult sr = stream.close()

		then:
		n == 23
		stream.rcount == n
		buf.get(5) == 0x3F

		//		cleanup:
		//		f.delete()
	}

	def "write unicode chars to stream"() {
		setup:
		Fileref fr = Fileref.createTemp(IGlk.FILEUSAGE_DATA)
		Stream stream = new FileStream(fr, IGlk.FILEMODE_WRITE, false)
		String s = "Test ${0x3B1 as char}x${0x201C as char}x${0x201D as char}.^Re${0xEB as char}l${0xEE as char}ty-${0xA9 as char}.^"

		when:
		s.toCharArray().each {
			stream.putCharUni(it as int)
		}
		stream.close()

		then:
		stream.wcount == 23
	}

	def createTempfile() {
		Fileref fileref_rw = Fileref.createTemp(IGlk.FILEUSAGE_DATA)
		Stream stream = new FileStream(fileref_rw, IGlk.FILEMODE_WRITE, false)
		stream.putString("The quick brown fox jumped over the lazy dog.")
		stream.putString(System.getProperty("line.separator"))
		stream.putString("Pop goes the weasel.")
		stream.close()
		return fileref_rw
	}

	def createTempfileUnicode() {
		Fileref fileref_rw = Fileref.createTemp(IGlk.FILEUSAGE_DATA)
		Stream stream = new FileStream(fileref_rw, IGlk.FILEMODE_WRITE, true)
		stream.putStringUni("The quick brown fox jumped over the lazy dog.")
		stream.putStringUni(System.getProperty("line.separator"))
		stream.putStringUni("Pop goes the weasel.")
		stream.close()
		return fileref_rw
	}

	def createTempfileUnicodePutChar() {
		Fileref fileref_rw = Fileref.createTemp(IGlk.FILEUSAGE_DATA)
		Stream stream = new FileStream(fileref_rw, IGlk.FILEMODE_WRITE, true)
		stream.putCharUni((int)"T")
		stream.putCharUni((int)"h")
		stream.putCharUni((int)"e")
		stream.close()
		return fileref_rw
	}
}
