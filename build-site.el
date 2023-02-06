(require 'ox-publish)

(setq org-html-validation-link nil
      org-html-head-include-scripts nil
      org-html-head-include-default-style nil
      org-html-head "<link rel=\"stylesheet\" href=\"https://cdn.simplecss.org/simple.min.css\" />")



;; Define the publishing project
(setq org-publish-project-alist
      (list
       (list
	"orgfiles"
        :recursive t
        :base-directory "./content"
        :publishing-directory "./public"
        :publishing-function 'org-html-publish-to-html
	:with-author nil
	:include-toc nil
        :section-numbers nil
	:time-stamp-file nil)))

;; Generate the site output
(org-publish-all t)

(message "Build complete!")
