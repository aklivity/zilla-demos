# it's a good idea to pin this, but for demo purposes we'll leave it as is
FROM node:19-alpine as build-stage

# automatically creates the dir and sets it as the current working dir
WORKDIR /usr/src/app

# System deps and app setup:
RUN apk --no-cache add 'build-base>=0.5' 'git>=2.38' \
&& npm install -g 'vite@^5.0'

COPY package.json ./
COPY package-lock.json ./

RUN npm install

# use a more specific COPY, as this will include files like `Dockerfile`, we don't really need inside our containers.
COPY . ./

RUN npm run build

# it's a good idea to pin this, but for demo purposes we'll leave it as is
FROM nginxinc/nginx-unprivileged:stable-alpine as production-stage

COPY --from=build-stage /usr/src/app/dist /usr/share/nginx/html

CMD ["nginx", "-g", "daemon off;"]
