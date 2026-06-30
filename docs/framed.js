function cleanFrame(iframe, selectors) {
    try {
        const doc = iframe.contentDocument || iframe.contentWindow.document;

        // Hide standard UI elements (headers, nav bars, search bars, etc.)
        selectors.forEach(selector => {
            const el = doc.querySelector(selector);
            if (el) el.style.display = 'none';
        });


        // If not a package-summary page → continue with allclasses-index style cleanup
        const tabs = doc.querySelector('.table-tabs');
        if (tabs) tabs.style.display = 'none';

        const captionSummary = doc.querySelectorAll('.caption');
        captionSummary.forEach(el => el.style.display = 'none');

        const captionHeaders = doc.querySelectorAll('.table-header.col-first');
        captionHeaders.forEach(el => el.style.display = 'none');

        const descHeaders = doc.querySelectorAll('.table-header.col-last');
        descHeaders.forEach(el => el.style.display = 'none');

        const descriptions = doc.querySelectorAll('.col-last');
        descriptions.forEach(el => el.style.display = 'none');

        var panel = doc.querySelector('#all-classes-table\\.tabpanel');
        if (!panel) panel = doc.querySelector('#class-summary');
        if (!panel) {
            panel = doc.querySelector('#all-packages-table');

            const rowClass = 'even-row-color';

            const colFirst = doc.createElement('div');
            colFirst.className = `col-first ${rowClass} all-packages-table-tab1`;
            const link = doc.createElement('a');
            link.href = 'allclasses-index.html';
            // link.target			= 'classes';
            link.textContent = 'All classes';
            colFirst.appendChild(link);

            const colLast = doc.createElement('div');
            colLast.className = `col-last ${rowClass} all-packages-table-tab1`;
            colLast.textContent = 'Dislpay all classes';

            panel.insertBefore(colLast, panel.firstChild);
            panel.insertBefore(colFirst, panel.firstChild);
        }
        if (!panel) return;

        const tabGroups = [
            {className: 'all-packages-table-tab1', label: 'Packages'},
            {className: 'all-classes-table-tab1', label: 'Interfaces'},
            {className: 'all-classes-table-tab2', label: 'Classes'},
            {className: 'all-classes-table-tab3', label: 'Enum Classes'},
            {className: 'class-summary-tab1', label: 'Interfaces'},
            {className: 'class-summary-tab2', label: 'Classes'},
            {className: 'class-summary-tab3', label: 'Enum Classes'},
        ];

        const fragments = document.createDocumentFragment();

        tabGroups.forEach(group => {
            const elements = Array.from(
                panel.querySelectorAll(`.col-first.${group.className}`)
            );

            if (elements.length === 0) return;

            const heading = doc.createElement('h2');
            heading.textContent = group.label;
            heading.style.margin = '1em 0 0.3em';
            heading.style.fontSize = '1.1em';
            heading.style.fontWeight = 'bold';
            heading.style.fontFamily = 'Segoe UI, sans-serif';
            heading.style.borderBottom = '1px solid #ccc';
            heading.style.paddingBottom = '0.2em';
            fragments.appendChild(heading);

            elements.forEach(el => {
                const sibling = el.nextElementSibling;
                fragments.appendChild(el);
                if (sibling && sibling.classList.contains('col-last')) {
                    sibling.style.display = 'none';
                }
            });
        });

        panel.innerHTML = '';
        panel.appendChild(fragments);

    } catch (e) {
        console.warn('Error cleaning iframe content:', e);
    }
}


const selectorsToHide = ['header', 'nav', '#navbar-top', '#navbar-bottom', '#search', '.header', '.skip-nav', 'footer', '#related-package-summary'];

function configurePackagesFrame(pkg, classesFrame, contentFrame) {
    cleanFrame(pkg, selectorsToHide);
    const doc = pkg.contentDocument || pkg.contentWindow.document;
    const links = doc.querySelectorAll('a[href]');//'a[href$="-summary.html"]'

    links.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const href = link.getAttribute('href');

            //if (href.includes("package-summary.html")) {
            classesFrame.setAttribute('src', href);
            contentFrame.setAttribute('src', href);
            //}
        });
    });
}

function configureClassesFrame(pkg, contentFrame) {
    cleanFrame(pkg, selectorsToHide);
    const doc = pkg.contentDocument || pkg.contentWindow.document;
    const links = doc.querySelectorAll('a[href$=".html"]:not([href$="package-summary.html"])');
    const pkgName = doc.querySelector('span.element-name');
    const packageName = pkgName ? pkgName.textContent.trim() : null;

    links.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            href = link.getAttribute('href');
            if (packageName != null) {
                href = packageName.replaceAll('.', '/') + '/' + href
            }
            contentFrame.setAttribute('src', href);
        });
    });
}


window.addEventListener('DOMContentLoaded', () => {
    const packagesFrame = document.getElementById('packages');
    const classesFrame = document.getElementById('classes');
    const contentFrame = document.getElementById('content');

    packagesFrame.addEventListener("load", () => {
        configurePackagesFrame(packagesFrame, classesFrame, contentFrame);
    });

    classesFrame.addEventListener("load", () => {
        configureClassesFrame(classesFrame, contentFrame);
    });

});
