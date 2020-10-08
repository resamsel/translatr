FROM node:12

WORKDIR /usr/src/translatr

COPY package*.json ./

RUN npm ci --only=production

COPY tsconfig.json ./

COPY apps/lets-generate ./apps/lets-generate
COPY libs ./libs
COPY dist/lets-generate ./

CMD ["node", "lets-generate"]
