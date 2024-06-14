# Multistage build
# 1. Stage: Build the app
FROM node:19-alpine
WORKDIR /opt/client/


# Add diretories with necessary config files
COPY . .

RUN npm install --save

ENTRYPOINT ["/opt/client/node_modules/.bin/sse-server"]
