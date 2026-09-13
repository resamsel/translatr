FROM node:24

WORKDIR /usr/src/translatr

COPY package*.json ./

RUN npm ci --omit=dev

COPY tsconfig.json ./

COPY apps/lets-generate ./apps/lets-generate
COPY libs ./libs
COPY dist/lets-generate ./

CMD ["node", "lets-generate"]
