#
# Copyright 2024 Aklivity Inc
#
# Licensed under the Aklivity Community License (the "License"); you may not use
# this file except in compliance with the License.  You may obtain a copy of the
# License at
#
#   https://www.aklivity.io/aklivity-community-license/
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OF ANY KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations under the License.
#

# Use a Node.js image
FROM node:18-alpine

# Install pnpm globally
RUN corepack enable && corepack prepare pnpm@latest --activate

# Set the working directory
WORKDIR /app

# Copy only dependency files first for layer caching
COPY package.json ./

# Install dependencies with pnpm
RUN pnpm install

# Copy the rest of your application
COPY . .

# Build the Next.js app
RUN pnpm build

# Expose the app port
EXPOSE 3000

# Run the app in production mode
CMD ["pnpm", "start"]
