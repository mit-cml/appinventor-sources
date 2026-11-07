(function () {
  let docs = [];
  let idx = null;
  let activeFilter = 'All';

  async function initSearch() {
    const container = document.getElementById('global-search');
    if (!container) return;

    container.innerHTML = `
      <div class="search-input-wrapper">
        <span class="fa fa-search search-icon"></span>
        <input type="text" id="global-search-input" placeholder="Search MIT App Inventor…" autocomplete="off" />
        <button type="button" id="search-clear-btn" class="search-clear-btn hidden">&times;</button>
      </div>
      <div id="global-search-results" class="global-search-dropdown hidden">
        <div class="search-filter-tabs">
          <button class="filter-tab active" data-category="All">All</button>
          <button class="filter-tab" data-category="Documentation">Docs</button>
          <button class="filter-tab" data-category="Tutorials">Tutorials</button>
          <button class="filter-tab" data-category="FAQs">FAQs</button>
          <button class="filter-tab" data-category="Examples">Examples</button>
        </div>
        <div id="search-results-content" class="search-results-content"></div>
      </div>
    `;

    const input = document.getElementById('global-search-input');
    const resultsDropdown = document.getElementById('global-search-results');
    const resultsContent = document.getElementById('search-results-content');
    const clearBtn = document.getElementById('search-clear-btn');
    const filterTabs = container.querySelectorAll('.filter-tab');

    try {
      const resp = await fetch('/static/search-index.json');
      if (resp.ok) {
        const data = await resp.json();
        docs = data.docs || [];

        // Build Lunr index if library is present
        if (typeof lunr !== 'undefined' && docs.length > 0) {
          idx = lunr(function () {
            this.ref('id');
            this.field('title', { boost: 10 });
            this.field('content');
            docs.forEach(doc => this.add(doc));
          });
        }
      }
    } catch (e) {
      console.warn('Could not load search index:', e);
    }

    function executeSearch(query) {
      if (!query) {
        resultsDropdown.classList.add('hidden');
        clearBtn.classList.add('hidden');
        return;
      }
      clearBtn.classList.remove('hidden');

      let matches = [];
      if (idx) {
        try {
          const hits = idx.search(`*${query}*`);
          matches = hits.map(hit => docs.find(d => d.id === hit.ref)).filter(Boolean);
        } catch (err) {
          // Fallback to substring match if query syntax causes Lunr parse error
          matches = fallbackSearch(query);
        }
      } else {
        matches = fallbackSearch(query);
      }

      // Filter by active category
      if (activeFilter !== 'All') {
        matches = matches.filter(d => (d.category || 'Documentation') === activeFilter);
      }

      renderResults(matches, query);
    }

    function fallbackSearch(query) {
      const q = query.toLowerCase();
      return docs.filter(doc => {
        const inTitle = (doc.title || '').toLowerCase().includes(q);
        const inCategory = (doc.category || '').toLowerCase().includes(q);
        const inContent = (doc.content || '').toLowerCase().includes(q);
        return inTitle || inCategory || inContent;
      });
    }

    function renderResults(results, query) {
      resultsDropdown.classList.remove('hidden');
      if (!results || results.length === 0) {
        resultsContent.innerHTML = `
          <div class="search-no-results">
            <p>No results found for "<strong>${escapeHtml(query)}</strong>"</p>
            <small>Try searching for Camera, Bluetooth, Variables, Canvas...</small>
          </div>
        `;
        return;
      }

      const groups = {
        Documentation: [],
        Tutorials: [],
        FAQs: [],
        Examples: []
      };

      results.forEach(doc => {
        const cat = doc.category || 'Documentation';
        if (groups[cat]) {
          groups[cat].push(doc);
        } else {
          groups.Documentation.push(doc);
        }
      });

      let html = '';
      for (const [catName, items] of Object.entries(groups)) {
        if (items.length === 0) continue;
        html += `<div class="search-category-group">`;
        html += `<div class="search-category-header"><span class="badge badge-secondary">${catName}</span></div>`;
        html += `<ul class="search-results-list">`;
        items.slice(0, 5).forEach(item => {
          const snippet = item.content ? escapeHtml(item.content.substring(0, 85).replace(/[\r\n#*`]+/g, ' ')) + '...' : '';
          html += `
            <li class="search-result-item">
              <a href="${item.url || '#'}" target="_self">
                <span class="search-item-title">${escapeHtml(item.title)}</span>
                ${snippet ? `<span class="search-item-snippet">${snippet}</span>` : ''}
              </a>
            </li>
          `;
        });
        html += `</ul></div>`;
      }

      resultsContent.innerHTML = html;
    }

    function escapeHtml(str) {
      return (str || '').replace(/[&<>"']/g, function (m) {
        return ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[m];
      });
    }

    input.addEventListener('input', () => executeSearch(input.value.trim()));

    clearBtn.addEventListener('click', () => {
      input.value = '';
      executeSearch('');
      input.focus();
    });

    filterTabs.forEach(tab => {
      tab.addEventListener('click', () => {
        filterTabs.forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        activeFilter = tab.getAttribute('data-category');
        executeSearch(input.value.trim());
      });
    });

    // Close on click outside
    document.addEventListener('click', (e) => {
      if (!container.contains(e.target)) {
        resultsDropdown.classList.add('hidden');
      }
    });

    // Close on escape key
    input.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        resultsDropdown.classList.add('hidden');
      }
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initSearch);
  } else {
    initSearch();
  }
})();
