(package-initialize)
(unless package-archive-contents
  (package-refresh-contents))

(package-install 'htmlize)

(require 'ox-publish)

(setq org-html-htmlize-output-type 'css)

;; Define the publishing project
(setq org-publish-project-alist
      (list
       (list
	"orgfiles"
	:html-head-extra (concat
		     "<link rel =\"stylesheet\" type= \"text/css\" href=\"./styles.css\">"
		     "<link rel=\"icon\" href=\"data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22><text y=%22.9em%22 font-size=%2290%22>👻</text></svg>\">") 
        :recursive t
	:html-head-include-default-style t
        :base-directory "./content"
        :publishing-directory "./"
        :publishing-function 'org-html-publish-to-html
	:with-author nil
	:html-head "<header><a href=\"./\">home</a></header>"
        :section-numbers nil
	:time-stamp-file nil)))

;; Generate the site output
(org-publish-all t)

(message "Build complete!")
