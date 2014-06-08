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
		buf.array()[-1] == 0
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
		buf.array()[-1] == 0
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

		then:
		c == "T"
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
}
