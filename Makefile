VERSION = 0.1.2.1
NV = cortado-$(VERSION)
TARBALL = $(NV).tar.gz

all: $(TARBALL) ovt mmjs

compile:
	ant compile

clean:
	rm -rf output/build

distclean:
	rm -rf output/dist
	-rm -f cortado.jar 
	-rm -f cortado.tgz 
	-rm *.jar

jar:
	ant jar

dist: $(TARBALL)

release:
	rm -f $(TARBALL)
	make dist
	md5sum $(TARBALL) > $(TARBALL).md5

$(TARBALL):
	rm -rf $(NV)
	mkdir $(NV)
	cp --parents \
		src/com/jcraft/jogg/*.java src/com/jcraft/jorbis/*.java \
		src/com/fluendo/player/*.java src/com/fluendo/utils/*.java \
		src/com/fluendo/jheora/*.java src/com/fluendo/codecs/*.java \
		src/com/fluendo/plugin/*.java \
		src/com/fluendo/examples/*.java \
		stubs/sun/audio/*.java \
		stubs/javax/sound/sampled/*.java \
		Makefile build.properties build.xml \
		LICENSE.cortado LICENSE.jheora LICENSE.smoke \
		ChangeLog HACKING README RELEASE TODO NEWS play \
		$(NV)/
	tar cvzf $@ $(NV)
	rm -rf $(NV)

cortado-ovt-$(VERSION).jar:
	ant -Dexclude=MultiPart,JPEG,Smoke,Mulaw jar
	cp output/dist/applet/cortado.jar $@

cortado-ot-$(VERSION).jar:
	ant -Dexclude=MultiPart,JPEG,Smoke,Mulaw,Vorbis jar
	cp output/dist/applet/cortado.jar $@

cortado-mmjs-$(VERSION).jar:
	ant -Dexclude=Ogg,Theora,Vorbis jar
	cp output/dist/applet/cortado.jar $@

ovt:	cortado-ovt-$(VERSION).jar
ot:	cortado-ot-$(VERSION).jar
mmjs:	cortado-mmjs-$(VERSION).jar
