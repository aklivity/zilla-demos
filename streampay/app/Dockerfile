
# build stage
FROM node:18.16-alpine as build-stage
RUN npm install -g @quasar/cli@2.2.1
# pnpm fetch does require only lockfile
WORKDIR /app
COPY package.json ./
COPY package-lock.json ./
RUN npm i
ADD . ./
RUN npm build

# run stage
FROM nginx:stable-alpine as run-stage
COPY --from=build-stage /app/dist/spa /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
