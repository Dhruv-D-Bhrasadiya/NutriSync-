# Ansible deployment notes

1. Install Ansible on the control node and run `ansible-galaxy collection install -r ansible/collections/requirements.yml`.
2. Copy `ansible/inventory/dev.example.ini` to the ignored `ansible/inventory/dev.ini`, replace the TEST-NET IP with a disposable Ubuntu 22.04+ host and set its SSH user.
3. Build and push a versioned image to a registry the target can access. Export `NUTRISYNC_IMAGE` as the full image reference and `NUTRISYNC_DB_PASSWORD` as a unique secret (at least 20 characters) on the control node. Prefer loading them from Ansible Vault or a secret manager. Do not commit or print them.
4. Validate:
   ```bash
   ansible -i ansible/inventory/dev.ini app -m ping
   ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/site.yml --syntax-check
   ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/site.yml --check --diff
   ```
5. Apply and rerun:
   ```bash
   ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/site.yml
   ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/site.yml
   ```
6. Inspect the second run for unintended changes. To roll back, set `NUTRISYNC_IMAGE` to the previous tested tag and re-run the playbook.

The playbook installs packages, creates the service user/directory, creates a private Docker network and persistent PostgreSQL volume, manages database/app containers, configures Nginx and checks both health endpoints. Validate module and OS compatibility on a disposable host before applying.
