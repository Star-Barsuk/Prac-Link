### Clone only server folder
```bash
git clone --depth 1 --no-checkout https://github.com/Star-Barsuk/Prac-Link.git ./prac-link-server
cd ./prac-link-server
git sparse-checkout init --cone
git sparse-checkout set "server"
git checkout
```
### Clone only client folder
```bash
git clone --depth 1 --no-checkout https://github.com/Star-Barsuk/Prac-Link.git ./prac-link-client
cd ./prac-link-client
git sparse-checkout init --cone
git sparse-checkout set "client"
git checkout
```
