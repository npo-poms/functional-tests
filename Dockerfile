FROM maven:3.8-jdk-11

RUN apt-get -y update && \
    apt-get -y upgrade && \
    curl -L https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb -o google-chrome-stable_current_amd64.deb && \
    apt-get -y install ./google-chrome-stable_current_amd64.deb && \
    rm -f ./google-chrome-stable_current_amd64.deb && \
    apt-get -y install chromium-driver firefox-esr libgstreamer1.0-dev gstreamer1.0-plugins-bad gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-ugly fonts-freefont-ttf fonts-freefont-otf && \
    mkdir /root/conf /fitnessetests

COPY settings.xml /root/.m2/settings.xml

WORKDIR /fitnessetests
EXPOSE 9090

CMD ["bash"]
