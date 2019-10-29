### Redeploy Dokku

```
git add . && git commit -m "redeploy" && git push dokku master
```

### Init Dokku
```
git init . && git remote add dokku dokku@[your ip server]:qldt-rss
git pull dokku master
```

### Create App
```
dokku apps:create qldt-rss
dokku domains:add qldt-rss [your domain] www.[your domain]
dokku redirect:set qldt-rss www.[your domain] [your domain] 301
```

##Config studentId, password