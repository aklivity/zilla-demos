# Multistage build
# 1. Stage: Build the app
FROM node:19-alpine as build-stage
WORKDIR /opt/client/

# System deps and app setup:
RUN apk --no-cache add 'build-base>=0.5' 'git>=2.38' \
    && npm install -g 'pnpm@^8.14'

# Add diretories with necessary config files
COPY pnpm-lock.yaml package.json /opt/client/

# Only install production.
RUN pnpm install --only=prod

COPY build /opt/client/build
COPY src /opt/client/src
COPY config /opt/client/config
COPY .eslintignore pnpm-lock.yaml cypress.config.js .babelrc .eslintrc.js dev.html package.json .postcssrc.js /opt/client/

RUN pnpm build

# 2. Stage: Start server with needed files only
FROM nginxinc/nginx-unprivileged:stable-alpine as production-stage

COPY --from=build-stage /opt/client/static /usr/share/nginx/html/static
COPY --from=build-stage /opt/client/index.html /usr/share/nginx/html/
