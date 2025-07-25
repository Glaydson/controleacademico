

# Stage 1: Build the Angular application
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install
COPY . .
RUN npm run build -- --configuration production

# Stage 2: Serve the application with Nginx
FROM nginx:alpine
# Copia sua configuração Nginx customizada
COPY nginx.conf /etc/nginx/nginx.conf 
COPY --from=build /app/dist/frontend/browser/. /usr/share/nginx/html
RUN chown -R nginx:nginx /usr/share/nginx/html
RUN chmod -R 755 /usr/share/nginx/html
EXPOSE 80 
CMD ["nginx", "-g", "daemon off;"]