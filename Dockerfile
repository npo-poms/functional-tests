FROM maven:3.9-eclipse-temurin-11

ARG NAME=fitnesse-runner

# sadly, this doesn't work on arm64 (macs!)

RUN apt-get -y update && \
    apt-get -y upgrade && apt-get -y install make ssh


RUN if [ `arch` == 'amd64' ] ; then  curl -L https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb -o google-chrome-stable_current_amd64.deb ;\
    apt-get -y install ./google-chrome-stable_current_amd64.deb ;\
    rm -f ./google-chrome-stable_current_amd64.deb ; fi

RUN  apt-get -y install libgstreamer1.0-dev gstreamer1.0-plugins-bad gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-ugly fonts-freefont-ttf fonts-freefont-otf  && \
    mkdir /root/conf /fitnessetests





ARG FIREFOX_VERSION=122.0
RUN apt-get update -qqy \
	&& apt-get -qqy install libgtk-3-0 libx11-xcb1 libdbus-glib-1-2 libxt6 libasound2 \
	&& rm -rf /var/lib/apt/lists/* /var/cache/apt/* \
	&& wget -q -O /tmp/firefox.tar.bz2 https://download-installer.cdn.mozilla.net/pub/firefox/releases/$FIREFOX_VERSION/linux-x86_64/en-US/firefox-$FIREFOX_VERSION.tar.bz2 \
	&& tar xjf /tmp/firefox.tar.bz2 -C /opt \
	&& rm /tmp/firefox.tar.bz2 \
	&& mv /opt/firefox /opt/firefox-$FIREFOX_VERSION \
	&& ln -s /opt/firefox-$FIREFOX_VERSION/firefox /usr/bin/firefox


COPY maven-settings.xml /root/.m2/settings.xml

WORKDIR /fitnessetests

CMD ["bash"]